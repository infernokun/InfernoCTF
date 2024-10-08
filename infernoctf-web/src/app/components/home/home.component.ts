import { Component, OnInit } from '@angular/core';
import { RoomService } from '../../services/room.service';
import { BehaviorSubject } from 'rxjs';
import { Room, RoomFormData } from '../../models/room.model';
import { CommonEditDialogService } from '../../services/common-edit-dialog.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {

  roomSubject = new BehaviorSubject<any[]>([]);
  rooms$ = this.roomSubject.asObservable();

  constructor(
    private roomService: RoomService,
    private commonEditDialogService: CommonEditDialogService
  ) { }

  public ngOnInit(): void {
    this.roomService.getRooms().subscribe((rooms) => {
      console.log('rooms', rooms);
      this.roomSubject.next(rooms);
    });
  }

  public createRoom() {
    try {
      const roomFormData = new RoomFormData();
      this.commonEditDialogService.openDialog<Room>(roomFormData, (room: any) => {
        console.log('room', room);
        /*this.roomService.createRoom(room).subscribe((res: any) => {
          console.log('createRoom', res);
          this.roomService.getRooms().subscribe((rooms) => {
            this.roomSubject.next(rooms);
          });
        });*/
      }).subscribe((res: any) => { });

    } catch (error) {
      console.error('createRoom error', error);
    }
  }
}
