import { Injectable } from '@angular/core';
import { EnvironmentService } from './environment.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiResponse } from '../models/api-response.model';
import { LoginResponseDTO } from '../models/dto/login-response.dto.model';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(
    private environmentService: EnvironmentService,
    private httpClient: HttpClient) { }

  // Built per call: EnvironmentService loads its config asynchronously, so a URL captured at
  // construction time could still be `undefined/...`.
  private get authUrl(): string {
    return `${this.environmentService.settings?.restUrl}/auth`;
  }

  public login(username: string, password: string): Observable<ApiResponse<LoginResponseDTO>> {
    // withCredentials so the browser stores the refresh cookie the response sets.
    return this.httpClient.post<ApiResponse<LoginResponseDTO>>(
      `${this.authUrl}/login`, { username, password }, { withCredentials: true });
  }

  /**
   * Nothing in the body: the grant is an httpOnly cookie the browser attaches itself.
   * `withCredentials` is what makes it do so when the API is on another origin.
   */
  public refresh(): Observable<ApiResponse<LoginResponseDTO>> {
    return this.httpClient.post<ApiResponse<LoginResponseDTO>>(
      `${this.authUrl}/token`, {}, { withCredentials: true });
  }

  /** A single-use ticket authenticating the WebSocket handshake. */
  public webSocketTicket(): Observable<ApiResponse<{ ticket: string }>> {
    return this.httpClient.get<ApiResponse<{ ticket: string }>>(`${this.authUrl}/ws-ticket`);
  }

  /** The server identifies the user from the bearer token, so no id is sent. */
  public logout(): Observable<void> {
    return this.httpClient.post<void>(`${this.authUrl}/logout`, {}, { withCredentials: true });
  }

  public register(username: string, password: string, email: string): Observable<ApiResponse<boolean>> {
    return this.httpClient.post<ApiResponse<boolean>>(
      `${this.authUrl}/register`, { username, password, email });
  }
}
