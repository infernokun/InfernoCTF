import { Injectable } from '@angular/core';
import { jwtDecode, JwtPayload } from 'jwt-decode';

const ACCESS_TOKEN_KEY = 'jwt';

/**
 * The single place that knows where tokens live and how to read them.
 *
 * Only the access token is kept here; the refresh grant is an httpOnly cookie that script
 * cannot read. Decoding goes through `jwt-decode` because JWT payloads are base64url and
 * `atob` throws on any payload containing `-` or `_`.
 *
 * No HttpClient dependency, so it is safe to inject into an HTTP_INTERCEPTORS provider
 * without creating a DI cycle.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  setAccessToken(accessToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  /** Only what this app can reach; the refresh cookie is dropped by the server. */
  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }

  decode<T = JwtPayload>(token: string | null): T | null {
    if (!token) {
      return null;
    }
    try {
      return jwtDecode<T>(token);
    } catch {
      return null;
    }
  }

  /** The user id, which the API puts in the `sub` claim. */
  userId(): string | undefined {
    return this.decode(this.accessToken)?.sub;
  }

  isExpired(token: string | null): boolean {
    const exp = this.decode(token)?.exp;
    return !exp || exp * 1000 <= Date.now();
  }

  hasValidAccessToken(): boolean {
    return !!this.accessToken && !this.isExpired(this.accessToken);
  }
}
