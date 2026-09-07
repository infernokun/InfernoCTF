import { Injectable, OnDestroy } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import {
  defer, distinctUntilChanged, EMPTY, map, retry, Subject, Subscription, switchMap,
} from 'rxjs';

import { ApiResponse } from '../models/api-response.model';
import { AuthService } from './auth.service';
import { EnvironmentService } from './environment.service';
import { LoginService } from './login.service';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService implements OnDestroy {
  private clientFacingSubject$ = new Subject<any>();
  private socket$?: WebSocketSubject<any>;
  private connection?: Subscription;

  constructor(
    private environmentService: EnvironmentService,
    private loginService: LoginService,
    private authService: AuthService) {

    // The handshake needs a ticket, and a ticket needs a session: connect only while signed
    // in, and drop the socket on logout.
    this.connection = toObservable(this.authService.payload).pipe(
      map(payload => !!payload),
      distinctUntilChanged(),
      switchMap(authenticated => authenticated ? this.stream$() : EMPTY),
    ).subscribe({
      next: (message) => {
        if (this.environmentService.settings?.production === false) {
          console.log('Got socket message', message);
        }
        if (!(message && message.type === 'heartbeat')) {
          this.clientFacingSubject$.next(message);
        }
      },
      error: (e) => console.error('WebSocket Error', e),
    });
  }

  /**
   * One ticket per attempt. `defer` matters: tickets are single-use and short-lived, so every
   * retry must fetch a fresh one rather than replay the first.
   */
  private stream$() {
    return defer(() => this.loginService.webSocketTicket()).pipe(
      switchMap((response: ApiResponse<{ ticket: string }>) => {
        this.socket$ = webSocket(
          `${this.baseUrl()}/socket-handler/update?ticket=${encodeURIComponent(response.data.ticket)}`);
        return this.socket$;
      }),
      retry({ delay: 5000 }),
    );
  }

  /**
   * `websocketUrl` is only set in the dev config, where the API is on another port. In a
   * container the page's own origin is the right answer anyway: nginx forwards
   * /socket-handler/ to the API.
   */
  private baseUrl(): string {
    const configured = this.environmentService.settings?.websocketUrl;
    if (configured) {
      return configured;
    }
    const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws';
    return `${scheme}://${window.location.host}`;
  }

  ngOnDestroy(): void {
    this.connection?.unsubscribe();
    this.disconnect();
  }

  public getSubject(): Subject<any> {
    return this.clientFacingSubject$;
  }

  public disconnect(): void {
    this.socket$?.complete();
    this.socket$ = undefined;
  }
}
