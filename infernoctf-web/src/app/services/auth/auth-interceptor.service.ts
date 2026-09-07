import {
  HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { catchError, Observable, switchMap, throwError } from 'rxjs';

import { TokenStorageService } from './token-storage.service';
import { AuthService } from '../auth.service';

const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/token'];

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private tokenStorage: TokenStorageService, private injector: Injector) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenStorage.accessToken;

    const authed = token ? this.withToken(req, token) : req;

    return next.handle(authed).pipe(
      catchError((error: unknown) => {
        if (!(error instanceof HttpErrorResponse) || error.status !== 401 || this.isAuthCall(req)) {
          return throwError(() => error);
        }

        // refreshSession() shares one in-flight refresh, so parallel 401s cause one call.
        return this.injector.get(AuthService).refreshSession().pipe(
          switchMap((newToken: string) => next.handle(this.withToken(req, newToken))),
        );
      }),
    );
  }

  private isAuthCall(req: HttpRequest<any>): boolean {
    return AUTH_ENDPOINTS.some(endpoint => req.url.includes(endpoint));
  }

  private withToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
}
