import { Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class RoomService extends BaseService {

  constructor(
    private httpClient: HttpClient,
    private environmentService: EnvironmentService,
    protected override authService: AuthService) {
    super(httpClient, authService);
  }

  getRooms() {
    return this.get<any>(this.environmentService.settings?.restUrl + '/room');
  }
}
