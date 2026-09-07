import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Subject, takeUntil, switchMap, filter, tap, catchError, of, EMPTY } from 'rxjs';
import { CTFService } from '../../../services/ctf.service';
import { CTFEntity } from '../../../models/ctf-entity.model';
import { DialogService } from '../../../services/dialog.service';
import { WebsocketService } from '../../../services/websocket.service';
import { AuthService } from '../../../services/auth.service';
import { ActivatedRoute } from '@angular/router';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-ctf-card',
  templateUrl: './ctf-card.component.html',
  styleUrl: './ctf-card.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CTFCardComponent implements OnInit, OnDestroy {
  // inject() rather than constructor parameters: the fields below read these during field
  // initialization, before parameter properties would be assigned.
  private readonly ctfService = inject(CTFService);
  private readonly dialogService = inject(DialogService);
  private readonly webSocketService = inject(WebsocketService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  /** Built here because toObservable() needs an injection context; ngOnInit is not one. */
  private readonly authLoading$ = toObservable(this.authService.loading);

  readonly challenges = signal<CTFEntity[]>([]);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);

  readonly loading = this.ctfService.loading;

  // Subject for handling component destruction
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.initializeComponent();
    this.subscribeToWebSocketMessages();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeComponent(): void {
    this.busy.set(true);
    this.error.set(null);

    // RxJS here: this coordinates two async sources and cancels in-flight work on change.
    this.authLoading$
      .pipe(
        filter(loading => !loading), // Wait until auth is not loading
        switchMap(() => this.route.params), // Switch to route params
        filter(params => !!params['room']), // Only proceed if room param exists
        tap(params => console.log('Route params:', params)),
        switchMap(params => this.loadChallenges(params['room'])),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (challenges) => {
          this.challenges.set(challenges);
          this.busy.set(false);
        },
        error: (error) => {
          console.error('Error loading challenges:', error);
          this.error.set('Failed to load challenges. Please try again.');
          this.busy.set(false);
        }
      });
  }

  private loadChallenges(roomId: string) {
    return this.ctfService.getChallengesByRoom(roomId)
      .pipe(
        tap(() => this.ctfService.setLoading(true)),
        catchError(error => {
          this.ctfService.setLoading(false);
          throw error;
        }),
        filter((response: ApiResponse<CTFEntity[]>) => !!response?.data),
        tap(() => this.ctfService.setLoading(false))
      )
      .pipe(
        // Extract data from response
        switchMap((response: ApiResponse<CTFEntity[]>) => 
          response.data ? of(response.data) : EMPTY
        )
      );
  }

  private subscribeToWebSocketMessages(): void {
    // getSubject(), not the raw socket: it survives reconnects, filters heartbeats, and
    // exists before a connection does.
    this.webSocketService.getSubject()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (message) => {
          console.log('WebSocket message received:', message);
          // Handle websocket message logic here
          this.handleWebSocketMessage(message);
        },
        error: (error) => {
          console.error('WebSocket error:', error);
        }
      });
  }

  private handleWebSocketMessage(message: any): void {
    // Add your websocket message handling logic here
    // For example, refresh challenges if needed
    // if (message.type === 'CHALLENGE_UPDATED') {
    //   this.refreshChallenges();
    // }
  }

  public openViewDialog(challenge: CTFEntity): void {
    if (!challenge) {
      console.warn('No challenge provided for view dialog');
      return;
    }

    this.dialogService.openViewDialog(challenge)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          console.log('View dialog result:', result);
          // Handle dialog result if needed
        },
        error: (error) => {
          console.error('Error opening view dialog:', error);
        }
      });
  }

  public openEditDialog(challenge: CTFEntity): void {
    if (!challenge) {
      console.warn('No challenge provided for edit dialog');
      return;
    }

    this.dialogService.openEditDialog(challenge)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          console.log('Edit dialog result:', result);
          // Handle dialog result - maybe refresh the challenge list
          if (result && result.updated) {
            this.refreshChallenges();
          }
        },
        error: (error) => {
          console.error('Error opening edit dialog:', error);
        }
      });
  }

  public refreshChallenges(): void {
    const currentRoom = this.route.snapshot.params['room'];
    if (currentRoom) {
      this.busy.set(true);
      this.loadChallenges(currentRoom)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (challenges) => {
            this.challenges.set(challenges);
            this.busy.set(false);
          },
          error: (error) => {
            console.error('Error refreshing challenges:', error);
            this.error.set('Failed to refresh challenges.');
            this.busy.set(false);
          }
        });
    }
  }

  public retry(): void {
    this.initializeComponent();
  }
}