import { Component, OnInit } from '@angular/core';
import { RoomService } from '../../services/room.service';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {

  roomSubject = new BehaviorSubject<any[]>([]);
  rooms$ = this.roomSubject.asObservable();

  constructor(private roomService: RoomService) { }

  ngOnInit(): void {
    this.roomService.getRooms().subscribe((rooms) => {
      console.log('rooms', rooms);
      this.roomSubject.next(rooms);
    });
  }
}
