import { Injectable, signal } from '@angular/core';
import { EnvironmentService } from './environment.service';
import { HttpClient } from '@angular/common/http';
import { CTFEntity } from '../models/ctf-entity.model';
import { Observable } from 'rxjs';
import { FlagAnswer } from '../models/flag-answer.model';
import { AuthService } from './auth.service';
import { BaseService } from './base.service';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class CTFService extends BaseService {
  private readonly loadingState = signal<boolean>(true);
  readonly loading = this.loadingState.asReadonly();

  setLoading(loading: boolean): void {
    this.loadingState.set(loading);
  }

  constructor(
    protected httpClient: HttpClient,
    private environmentService: EnvironmentService
  ) { 
    super(httpClient);
  }

  getAllChallenges(): Observable<ApiResponse<CTFEntity[]>> {
    return this.get<ApiResponse<CTFEntity[]>>(this.environmentService.settings?.restUrl + '/ctf-entity');
  }

  getChallengesByRoom(roomId: string): Observable<ApiResponse<CTFEntity[]>> {
    return this.get<ApiResponse<CTFEntity[]>>(this.environmentService.settings?.restUrl + '/ctf-entity/by?room=' + roomId);
  }

  answerChallenge(flag: FlagAnswer): Observable<ApiResponse<any>> {
    return this.post<ApiResponse<any>>(this.environmentService.settings?.restUrl + '/answer', flag);
  }

  answerChallengeCheck(ctfEntity: CTFEntity): Observable<ApiResponse<any>> {
    return this.get<ApiResponse<any>>(this.environmentService.settings?.restUrl + `/answer/check?ctfEntityId=${ctfEntity.id}`);
  }
}
