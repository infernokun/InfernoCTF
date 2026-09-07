import { computed, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, map, Observable, of, shareReplay, switchMap, throwError } from 'rxjs';

import { ApiResponse } from '../models/api-response.model';
import { LoginResponseDTO } from '../models/dto/login-response.dto.model';
import { Role } from '../models/enums/role.enum';
import { User } from '../models/user.model';
import { LoginService } from './login.service';
import { TokenStorageService } from './auth/token-storage.service';
import { UserService } from './user.service';

export interface UserPayload {
  user: User;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly payloadState = signal<UserPayload | undefined>(undefined);
  private readonly loadingState = signal<boolean>(true);

  readonly payload = this.payloadState.asReadonly();
  readonly user = computed(() => this.payloadState()?.user);
  readonly loading = this.loadingState.asReadonly();

  /** The refresh currently in flight, shared so parallel 401s cause exactly one refresh call. */
  private refreshInFlight$: Observable<string> | null = null;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private userService: UserService,
    private tokenStorage: TokenStorageService) { }

  /**
   * Resolves whether there is a usable session, refreshing the access token if it has expired.
   * Loading the user is the server-side check: that endpoint needs a valid bearer token, so a
   * successful response proves the token is good.
   */
  isAuthenticated(): Observable<boolean> {
    const token = this.tokenStorage.accessToken;
    if (!token) {
      this.clearSession();
      return of(false);
    }

    const usableToken$ = this.tokenStorage.isExpired(token) ? this.refreshSession() : of(token);

    return usableToken$.pipe(
      switchMap((validToken: string) => {
        const userId = this.tokenStorage.decode(validToken)?.sub;
        if (!userId) {
          this.clearSession();
          return of(false);
        }
        return this.userService.getUserById(userId).pipe(
          map((user: User | undefined) => {
            if (!user) {
              this.clearSession();
              return false;
            }
            this.setPayload(user, validToken);
            return true;
          }),
        );
      }),
      catchError(() => {
        this.clearSession();
        return of(false);
      }),
    );
  }

  /**
   * Exchanges the refresh grant for a new access token. Concurrent callers share one HTTP
   * call: grants are single-use, so parallel refreshes would invalidate each other.
   */
  refreshSession(): Observable<string> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    // Nothing to pass: the grant is a cookie the browser attaches itself. If it is missing
    // the server answers 400 and the catch below clears the session.
    this.refreshInFlight$ = this.loginService.refresh().pipe(
      map((response: ApiResponse<LoginResponseDTO>) => {
        const data = response?.data;
        if (!data?.jwt) {
          throw new Error('Refresh response did not contain a token');
        }
        this.tokenStorage.setAccessToken(data.jwt);
        if (data.user) {
          this.setPayload(new User(data.user), data.jwt);
        }
        return data.jwt;
      }),
      catchError((error: unknown) => {
        this.clearSession();
        return throwError(() => error);
      }),
      finalize(() => { this.refreshInFlight$ = null; }),
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    return this.refreshInFlight$;
  }

  /** Reuses the resolved session so guarded navigations do not re-fetch the user each time. */
  ensureAuthenticated(): Observable<boolean> {
    if (this.payload() && this.tokenStorage.hasValidAccessToken()) {
      return of(true);
    }
    return this.isAuthenticated();
  }

  logout(): void {
    // Clear locally whatever the server says.
    const finish = () => {
      this.clearSession();
      this.router.navigate(['/']);
    };

    if (!this.tokenStorage.accessToken) {
      finish();
      return;
    }

    this.loginService.logout().subscribe({ next: finish, error: finish });
  }

  /** @param jwt the encoded access token - not its decoded claims. */
  setPayload(user: User, jwt: string): void {
    this.payloadState.set({ user, token: jwt });
  }

  setLoading(loading: boolean): void {
    this.loadingState.set(loading);
  }

  setUser(user: User): void {
    this.payloadState.update(current =>
      current && current.user.id === user.id ? { ...current, user } : current);
  }

  isAdmin(): boolean {
    const role = this.user()?.role;
    return role === Role.ADMIN || role === Role.DEVELOPER;
  }

  private clearSession(): void {
    this.tokenStorage.clear();
    this.payloadState.set(undefined);
  }
}
