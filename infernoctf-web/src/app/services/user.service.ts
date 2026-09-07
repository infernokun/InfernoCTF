import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { User } from '../models/user.model';
import { ApiResponse } from '../models/api-response.model';
import { EnvironmentService } from './environment.service';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root'
})
export class UserService extends BaseService {
  private readonly usersState = signal<User[]>([]);
  private readonly loggedInUserState = signal<User | undefined>(undefined);

  readonly users = this.usersState.asReadonly();
  readonly loggedInUser = this.loggedInUserState.asReadonly();

  constructor(
    protected httpClient: HttpClient,
    private environmentService: EnvironmentService) {
    super(httpClient);
  }

  setLoggedInUser(user: User): void {
    this.loggedInUserState.set(user);
  }

  getUserById(id: string): Observable<User | undefined> {
    return this.get<ApiResponse<User>>(this.environmentService.settings?.restUrl + '/user/by?id=' + id)
      .pipe(
        map((response: ApiResponse<User>) => new User(response.data))
      );
  }

  getAllUsers(): Observable<User[]> {
    return this.get<ApiResponse<User[]>>(this.environmentService.settings?.restUrl + '/user').pipe(
      map((response: ApiResponse<User[]>) => response.data.map((user) => new User(user))),
      tap((users) => this.usersState.set(users))
    );
  }
}