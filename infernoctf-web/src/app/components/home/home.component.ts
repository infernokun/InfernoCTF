import { Component, OnInit, OnDestroy } from '@angular/core';
import { RoomService } from '../../services/room.service';
import { BehaviorSubject, Observable, Subject, finalize, takeUntil } from 'rxjs';
import { Room, RoomFormData } from '../../models/room.model';
import { CommonEditDialogService } from '../../services/common-edit-dialog.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  standalone: false
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private roomsSubject = new BehaviorSubject<Room[]>([]);
  
  rooms$: Observable<Room[]> = this.roomsSubject.asObservable();
  isLoading = false;

  constructor(
    private roomService: RoomService,
    private commonEditDialogService: CommonEditDialogService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadRooms(): void {
    this.isLoading = true;
    this.roomService.getAllRooms()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (rooms) => {
          this.roomsSubject.next(rooms);
        },
        error: (error) => {
          console.error('Failed to load rooms', error);
          this.showError('Failed to load rooms. Please try again later.');
        }
      });
  }

  createRoom(): void {
    try {
      const roomFormData = new RoomFormData();
      this.commonEditDialogService
        .openDialog<Room>(roomFormData, this.handleRoomCreation.bind(this))
        .pipe(takeUntil(this.destroy$))
        .subscribe();
    } catch (error) {
      console.error('Error opening room creation dialog', error);
      this.showError('Could not open room creation dialog. Please try again.');
    }
  }

  private handleRoomCreation(room: Room): void {
    if (!room) return;
    
    this.isLoading = true;
    /*this.roomService.createRoom(room)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (createdRoom) => {
          this.showSuccess(`Room "${createdRoom.name}" successfully created`);
          this.loadRooms();
        },
        error: (error) => {
          console.error('Failed to create room', error);
          this.showError('Failed to create room. Please try again.');
        }
      });*/
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}