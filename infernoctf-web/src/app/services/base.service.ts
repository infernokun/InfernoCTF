import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BaseService {
  constructor(protected http: HttpClient) { }

  protected get<T>(url: string, options?: { 
    headers?: HttpHeaders | { [header: string]: string | string[] }; 
    params?: HttpParams | { [param: string]: string | number | boolean | ReadonlyArray<string | number | boolean> };
  }): Observable<T> {
    return this.makeRequest<T>('GET', url, null, options);
  }

  protected post<T>(url: string, body: any | null, options?: { 
    headers?: HttpHeaders | { [header: string]: string | string[] };
  }): Observable<T> {
    return this.makeRequest<T>('POST', url, body, options);
  }

  protected put<T>(url: string, body: any | null, options?: { 
    headers?: HttpHeaders | { [header: string]: string | string[] };
  }): Observable<T> {
    return this.makeRequest<T>('PUT', url, body, options);
  }

  protected delete<T>(url: string, options?: { 
    headers?: HttpHeaders | { [header: string]: string | string[] };
  }): Observable<T> {
    return this.makeRequest<T>('DELETE', url, null, options);
  }

  protected makeRequest<T>(method: string, url: string, body: any | null, options?: { 
    headers?: HttpHeaders | { [header: string]: string | string[] };
    params?: HttpParams | { [param: string]: string | number | boolean | ReadonlyArray<string | number | boolean> };
  }): Observable<T> {
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

  // Token validation moved here from the AuthService-dependent method
  protected isValidToken(token: string): boolean {
    try {
      const tokenDecode = atob(token.split('.')[1]);
      if (tokenDecode) {
        const tokenObject = JSON.parse(tokenDecode);
        if (tokenObject.exp) {
          const expirationDate = new Date(tokenObject.exp * 1000);
          return expirationDate > new Date();
        }
      }
    } catch (e) {
      console.error('Token validation error:', e);
    }
    return false;
  }
}