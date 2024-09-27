import { Injectable } from '@angular/core';
import { EnvironmentService } from './environment.service';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user.model';
import { BaseService } from './base.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService extends BaseService {
  public loadingSubject = new BehaviorSubject<boolean>(true);
  loading$ = this.loadingSubject.asObservable();

  constructor(
    private environmentService: EnvironmentService,
    protected httpClient: HttpClient,
    protected override authService: AuthService) {
    super(httpClient, authService);
  }

  public getUsers(): Observable<User[]> {
    return this.get<User[]>(`${this.environmentService.settings?.restUrl}/user/all`);
  }
}
