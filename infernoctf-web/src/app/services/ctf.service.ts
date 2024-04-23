import { Injectable } from '@angular/core';
import { EnvironmentService } from './environment.service';
import { HttpClient } from '@angular/common/http';
import { CTFEntity } from '../models/ctf-entity.model';
import { BehaviorSubject, EMPTY, Observable, switchMap } from 'rxjs';
import { FlagAnswer } from '../models/flag-answer.model';
import { AuthService } from './auth.service';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root'
})
export class CTFService extends BaseService {
  public loadingSubject = new BehaviorSubject<boolean>(true);
  loading$ = this.loadingSubject.asObservable();

  constructor(
    protected httpClient: HttpClient,
    private environmentService: EnvironmentService,
    protected override authService: AuthService
  ) { 
    super(httpClient, authService);
  }

  getAllChallenges(): Observable<CTFEntity[]> {
    //return this.httpClient.get<CTFEntity[]>(this.environmentService.settings?.restUrl + '/ctf-entity');
    return this.get<CTFEntity[]>(this.environmentService.settings?.restUrl + '/ctf-entity');
  }

  getChallengesByRoom(roomId: string): Observable<CTFEntity[]> {
    return this.get<CTFEntity[]>(this.environmentService.settings?.restUrl + '/ctf-entity/room/' + roomId);
  }

  answerChallenge(flag: FlagAnswer): Observable<any> {
    //return this.httpClient.post<FlagAnswer>(this.environmentService.settings?.restUrl + '/answer', flag);
    return this.post<FlagAnswer>(this.environmentService.settings?.restUrl + '/answer', flag);
  }

  answerChallengeCheck(ctfEntity: CTFEntity): Observable<any> {
    //return this.httpClient.post<FlagAnswer>(this.environmentService.settings?.restUrl + '/answer', flag);
    return this.get<any>(this.environmentService.settings?.restUrl + `/answer/check?ctfEntityId=${ctfEntity.id}`);
  }
}
