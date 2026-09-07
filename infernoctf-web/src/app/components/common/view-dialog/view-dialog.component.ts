import { ChangeDetectionStrategy, Component, Inject, signal } from '@angular/core';
import { CTFEntity } from '../../../models/ctf-entity.model';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CTFService } from '../../../services/ctf.service';
import { FlagAnswer } from '../../../models/flag-answer.model';
import { AuthService } from '../../../services/auth.service';
import { catchError, of, throwError } from 'rxjs';
import { ApiResponse } from '../../../models/api-response.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-view-dialog',
  templateUrl: './view-dialog.component.html',
  styleUrl: './view-dialog.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewDialogComponent {
  viewedChallenge!: CTFEntity;

  readonly answer = signal('');
  readonly isAnswered = signal(false);

  constructor(
    private ctfService: CTFService,
    private authService: AuthService,
    @Inject(MAT_DIALOG_DATA) public data: CTFEntity,
    private dialogRef: MatDialogRef<ViewDialogComponent>) {
    this.viewedChallenge = { ...data };
  }

  ngOnInit(): void {
    console.log("opened dialog");

    this.ctfService.answerChallengeCheck(this.viewedChallenge).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          // Return a default response or empty observable
          return error.error as ApiResponse<any> ? of(error.error) : of({ data: null });
        }
        // Re-throw other errors
        return throwError(() => error);
      })
    ).subscribe((response: ApiResponse<any>) => {
      console.log('check answer response', response);
      
      if (response.data) {
        console.log('is answered', response);
        if (response.data.correct === true) {
          this.isAnswered.set(true);
          const ans: any[] = response.data.answers;
          this.answer.set(ans[ans.length - 1]);
          return;
        }
        if (response.data.attempts == this.viewedChallenge.maxAttempts) {
          this.isAnswered.set(true);
          return;
        }
      } else {
        // Handle the 404 case - challenge not answered yet
        this.isAnswered.set(false);
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  checkAnswer(challenge: CTFEntity): void {
    const payload = this.authService.payload();
    if (!payload) {
      return;
    }

    const flag: FlagAnswer = new FlagAnswer(this.answer(), payload.user.username!, challenge.id!);
    this.ctfService.answerChallenge(flag).subscribe((response: ApiResponse<any>) => {
      if (!response.data) {
        return;
      }
      if (response.data.correct === true || response.data.attempts == this.viewedChallenge.maxAttempts) {
        this.isAnswered.set(true);
        return;
      }
      this.isAnswered.set(response.data.correct);
    });
  }
}
