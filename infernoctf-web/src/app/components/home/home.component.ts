import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RoomService } from '../../services/room.service';
import { Subject, debounceTime, distinctUntilChanged, finalize, map, startWith, takeUntil } from 'rxjs';
import { Room, RoomFormData } from '../../models/room.model';
import { CommonEditDialogService } from '../../services/common-edit-dialog.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiResponse } from '../../models/api-response.model';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  readonly isLoading = signal(false);

  // Form controls
  searchControl = new FormControl('');

  /** Debouncing is time-based, so the keystroke stream stays RxJS; toSignal takes it from there. */
  private readonly searchTerm = toSignal(
    this.searchControl.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      map(term => (term || '').toLowerCase().trim()),
    ),
    { initialValue: '' });

  /** Recomputes only when the room list or the search term changes. */
  readonly filteredRooms = computed(() =>
    this.filterRooms(this.roomService.rooms(), this.searchTerm()));

  readonly trackByRoomId = (index: number, room: Room): string => room.id || index.toString();

  constructor(
    private roomService: RoomService,
    private commonEditDialogService: CommonEditDialogService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadRooms();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Filter rooms based on search term
   */
  private filterRooms(rooms: Room[], searchTerm: string): Room[] {
    if (!searchTerm) {
      return rooms;
    }

    return rooms.filter(room => 
      room.name?.toLowerCase().includes(searchTerm) ||
      room.creator?.toLowerCase().includes(searchTerm) ||
      room.facilitators?.some(facilitator => 
        facilitator.toLowerCase().includes(searchTerm)
      ) ||
      room.surroundTag?.toLowerCase().includes(searchTerm)
    );
  }

  /**
   * Load all rooms from the server
   */
  loadRooms(): void {
    this.setLoading(true);
    
    this.roomService.getAllRooms()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.setLoading(false))
      )
      .subscribe({
        next: (response) => this.handleRoomsLoaded(response),
        error: (error) => this.handleLoadRoomsError(error)
      });
  }

  /**
   * Handle successful rooms loading
   */
  private handleRoomsLoaded(response: ApiResponse<Room[]>): void {
    if (!response?.data) {
      console.warn('No room data received from server', response);
      this.roomService.addRooms([]);
      return;
    }

    try {
      // Convert server objects to Room instances if needed
      const rooms = Array.isArray(response.data) 
        ? response.data.map(roomData => new Room(roomData))
        : [];
      
      this.roomService.addRooms(rooms);
      console.log(`Successfully loaded ${rooms.length} rooms`);
      
      if (rooms.length === 0) {
        this.showInfo('No rooms found. Create your first room to get started!');
      }
    } catch (error) {
      console.error('Error processing room data:', error);
      this.showError('Error processing room data. Please refresh the page.');
    }
  }

  /**
   * Handle rooms loading error
   */
  private handleLoadRoomsError(error: any): void {
    console.error('Failed to load rooms:', error);
    
    const errorMessage = this.getErrorMessage(error);
    this.showError(`Failed to load rooms: ${errorMessage}`);
    
    // Set empty array on error to prevent template issues
    this.roomService.addRooms([]);
  }

  /**
   * Open room creation dialog
   */
  createRoom(): void {
    if (this.isLoading()) {
      this.showWarning('Please wait for the current operation to complete.');
      return;
    }

    try {
      const roomFormData = new RoomFormData();
      
      this.commonEditDialogService
        .openDialog<Room>(roomFormData, this.handleRoomCreation.bind(this))
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          error: (error) => {
            console.error('Error with room creation dialog:', error);
            this.showError('Could not process room creation. Please try again.');
          }
        });
    } catch (error) {
      console.error('Error opening room creation dialog:', error);
      this.showError('Could not open room creation dialog. Please try again.');
    }
  }

  /**
   * Handle room creation from dialog
   */
  private handleRoomCreation(room: Room): void {
    if (!room) {
      console.warn('No room data provided for creation');
      return;
    }

    // Validate room data
    if (!room.name?.trim()) {
      this.showError('Room name is required.');
      return;
    }
    
    this.setLoading(true);
    
    this.roomService.createRoom(room)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.setLoading(false))
      )
      .subscribe({
        next: (response) => this.handleRoomCreated(response),
        error: (error) => this.handleCreateRoomError(error)
      });
  }

  /**
   * Handle successful room creation
   */
  private handleRoomCreated(response: ApiResponse<Room>): void {
    if (!response?.data) {
      this.showError('Room creation failed: No data returned from server.');
      return;
    }

    try {
      const newRoom = new Room(response.data);
      this.roomService.addNewRoom(newRoom);
      this.showSuccess(`Room "${newRoom.name}" successfully created!`);
      
      // Clear search to show the new room
      this.searchControl.setValue('');
      
      console.log('Room successfully created:', newRoom);
    } catch (error) {
      console.error('Error processing created room:', error);
      this.showError('Room was created but there was an error displaying it. Please refresh the page.');
    }
  }

  /**
   * Handle room creation error
   */
  private handleCreateRoomError(error: any): void {
    console.error('Failed to create room:', error);
    
    const errorMessage = this.getErrorMessage(error);
    this.showError(`Failed to create room: ${errorMessage}`);
  }

  /**
   * Refresh rooms data
   */
  refreshRooms(): void {
    this.loadRooms();
  }

  /**
   * Clear search filter
   */
  clearSearch(): void {
    this.searchControl.setValue('');
  }

  /**
   * Get user-friendly error message
   */
  private getErrorMessage(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.message) {
      return error.message;
    }
    if (error?.status === 0) {
      return 'Network connection error. Please check your internet connection.';
    }
    if (error?.status >= 500) {
      return 'Server error. Please try again later.';
    }
    if (error?.status === 404) {
      return 'Service not found. Please contact support.';
    }
    if (error?.status === 403) {
      return 'Access denied. Please check your permissions.';
    }
    return 'An unexpected error occurred. Please try again.';
  }

  /**
   * Set loading state
   */
  private setLoading(loading: boolean): void {
    this.isLoading.set(loading);
  }

  /**
   * Show success message
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      panelClass: ['success-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }

  /**
   * Show error message
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 6000,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }

  /**
   * Show warning message
   */
  private showWarning(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      panelClass: ['warning-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }

  /**
   * Show info message
   */
  private showInfo(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['info-snackbar'],
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }
}