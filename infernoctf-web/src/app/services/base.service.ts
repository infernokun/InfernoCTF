import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpClient, HttpContext, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BaseService {

  constructor(protected http: HttpClient, protected authService: AuthService) { }

  protected get<T>(url: string, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; }; params?: HttpParams | { [param: string]: string | number | boolean | ReadonlyArray<string | number | boolean>; }}): Observable<T> {
    return this.requestWithTokenRefresh('GET', url, null, options);
  }

  protected post<T>(url: string, body: any | null, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; };}): Observable<T> {
    return this.requestWithTokenRefresh('POST', url, body, options);
  }

  protected put<T>(url: string, body: any | null, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; };}): Observable<T> {
    return this.requestWithTokenRefresh('PUT', url, body, options);
  }

  protected delete<T>(url: string, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; };}): Observable<T> {
    return this.requestWithTokenRefresh('DELETE', url, null, options);
  }

  private requestWithTokenRefresh<T>(method: string, url: string, body: any | null, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; };}): Observable<T> {
    const token = localStorage.getItem('jwt');
    if (!token) {
      return throwError(() => new Error('No token available'));
    }
  
    if (!this.isValidToken(token)) {
      console.log('Token expired while logged in and making a request!!! Revalidating...');
      return this.authService.revalidateToken(token).pipe(
        switchMap((successful) => {
          console.log('this is a test');
          if (successful) {
            console.log('Token revalidated gotten');
            return this.makeRequest<T>(method, url, body, options);
          } else {
            console.log('Token revalidation failed');
            //this.authService.logout();
            return throwError(() => new Error('Token revalidation failed'));          }
        }),
        catchError(error => {
          // Handle refresh token failure (e.g., logout user)
          return throwError(() => error);
        })
      );
    } else {
      return this.makeRequest<T>(method, url, body, options);
    }
  }
  

  private makeRequest<T>(method: string, url: string, body: any | null, options?: { headers?: HttpHeaders | { [header: string]: string | string[]; };}): Observable<T> {
    const requestOptions = {
      body: body,
      ...options
    };

    return this.http.request<T>(method, url, requestOptions).pipe(
      catchError((error: HttpErrorResponse) => {
          return throwError(() => error);
      })
    );
  }

  private isValidToken(token: string): boolean {
    const tokenDecode = atob(token.split('.')[1]);
    if (tokenDecode) {
      const tokenObject = JSON.parse(tokenDecode);
      if (tokenObject.exp) {
        const expirationDate = new Date(tokenObject.exp * 1000);
        return expirationDate > new Date();
      }
    }
    return false;
  }
}
