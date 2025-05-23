import { Component, Inject } from '@angular/core';
import { CTFEntity } from '../../../models/ctf-entity.model';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CTFService } from '../../../services/ctf.service';
import { FlagAnswer } from '../../../models/flag-answer.model';
import { AuthService, UserPayload } from '../../../services/auth.service';
import { BehaviorSubject, catchError, Observable, of, take, throwError } from 'rxjs';
import { ApiResponse } from '../../../models/api-response.model';
import { HttpErrorResponse } from '@angular/common/module.d-CnjH8Dlt';

@Component({
  selector: 'app-view-dialog',
  templateUrl: './view-dialog.component.html',
  styleUrl: './view-dialog.component.scss',
  standalone: false
})
export class ViewDialogComponent {
  viewedChallenge!: CTFEntity;
  answer: string = '';

  isAnswered: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  isAnswered$: Observable<boolean> = this.isAnswered.asObservable();

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
          this.isAnswered.next(true);
          const ans: any[] = response.data.answers;
          this.answer = ans[ans.length - 1];
          return;
        }
        if (response.data.attempts == this.viewedChallenge.maxAttempts) {
          this.isAnswered.next(true);
          return;
        }
      } else {
        // Handle the 404 case - challenge not answered yet
        this.isAnswered.next(false);
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  checkAnswer(challenge: CTFEntity): void {
    this.authService.payload$.pipe(take(1)).subscribe((payload: UserPayload | undefined) => {
      if (!payload) {
        return;
      }
      const flag: FlagAnswer = new FlagAnswer(this.answer, payload.user.username!, challenge.id!);
      this.ctfService.answerChallenge(flag).subscribe((response: ApiResponse<any>) => {
        if (response.data) {
          if (response.data.correct === true) {
            this.isAnswered.next(true);
            console.log('correct!!!');
            return;
          }

          if (response.data.attempts == this.viewedChallenge.maxAttempts) {
            this.isAnswered.next(true);
            console.log('max attempts reached');
            return;
          }
          this.isAnswered.next(response.data.correct);
          console.log('answer response', response);
        }
      });
    });
  }
}
