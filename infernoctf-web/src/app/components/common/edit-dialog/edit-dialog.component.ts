import { Component, Inject } from '@angular/core';
import { CTFEntity } from '../../../models/ctf-entity.model';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-edit-dialog',
  templateUrl: './edit-dialog.component.html',
  styleUrl: './edit-dialog.component.scss',
  standalone: false
})
export class EditDialogComponent {
  editedChallenge!: CTFEntity;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: CTFEntity,
    private dialogRef: MatDialogRef<EditDialogComponent>) {
      this.editedChallenge = { ...data };
    }

    closeDialog(): void {
      this.dialogRef.close();
      console.log('Dialog closed');
    }
}