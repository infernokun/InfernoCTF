import { Injectable, signal } from '@angular/core';
import { BaseService } from './base.service';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { EnvironmentService } from './environment.service';
import { Room } from '../models/room.model';
import { ApiResponse } from '../models/api-response.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoomService extends BaseService {

  private readonly roomsState = signal<Room[]>([]);
  readonly rooms = this.roomsState.asReadonly();

  constructor(
    private httpClient: HttpClient,
    private environmentService: EnvironmentService) {
    super(httpClient);
  }

  getAllRooms(): Observable<ApiResponse<Room[]>> {
    return this.get<ApiResponse<Room[]>>(this.environmentService.settings?.restUrl + '/room');
  }

  createRoom(room: Room): Observable<ApiResponse<Room>> {
    return this.post<ApiResponse<Room>>(this.environmentService.settings?.restUrl + '/room', room);
  }

  addNewRoom(room: Room): void {
    this.roomsState.update(rooms => [...rooms, room]);
  }

  addRooms(rooms: Room[]): void {
    this.roomsState.set(rooms ?? []);
  }
}
