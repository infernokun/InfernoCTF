import { Component } from '@angular/core';
import { CTFService } from '../../../services/ctf.service';
import { CTFEntity } from '../../../models/ctf-entity.model';
import { DialogService } from '../../../services/dialog.service';
import { WebsocketService } from '../../../services/websocket.service';
import { AuthService } from '../../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-ctf-card',
  templateUrl: './ctf-card.component.html',
  styleUrl: './ctf-card.component.scss',
  standalone: false
})
export class CTFCardComponent {
  public challenges: CTFEntity[] = [];
  public busy = false;

  loading$ = this.ctfService.loading$;

  constructor(
    private ctfService: CTFService,
    private dialogService: DialogService,
    private webSocketService: WebsocketService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {
    this.busy = true;
    this.route.params.subscribe((params) => {
      if (params['room']) {
        console.log('params', params);

        this.ctfService.getChallengesByRoom(params['room']).subscribe((response: ApiResponse<CTFEntity[]>) => {
          if (response.data) {
            this.challenges = response.data;
            this.busy = false;
            this.ctfService.loadingSubject.next(false);

            console.log('ctfEntities', response);
          }
        });
      }
    });

    this.authService.loading$.subscribe((loading) => {
      if (!loading) {
        this.ctfService.getAllChallenges().subscribe((response: ApiResponse<CTFEntity[]>) => {
          if (response.data) {
            this.challenges = response.data;
            this.ctfService.loadingSubject.next(false);
          }
        });

        this.webSocketService.getMessage().subscribe((message) => {
          console.log('message', message);
        });
      }
    });
  }

  openViewDialog(challengeFormData: CTFEntity) {
    this.dialogService.openViewDialog(challengeFormData).subscribe((res) => {
      console.log('wtf, what', res);
    });
  }

  openEditDialog(challengeFormData: CTFEntity) {
    this.dialogService.openEditDialog(challengeFormData).subscribe((res) => {
      console.log('wtf, what', res);
    });
  }
}
