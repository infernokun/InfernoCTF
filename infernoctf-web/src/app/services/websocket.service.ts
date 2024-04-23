import { Injectable, OnDestroy } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService implements OnDestroy {
  private socket$: WebSocketSubject<any>;

  constructor(private environmentService: EnvironmentService) {
    const wsUrl = `${this.environmentService.settings?.websocketUrl}/socket`;
    this.socket$ = webSocket(wsUrl);
  }

   ngOnInit() {
    
   }

   ngOnDestroy() {
    this.socket$.complete();
   }

   public getMessage(): WebSocketSubject<any> {
    return this.socket$;
   }
}
