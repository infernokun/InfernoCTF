import { Injectable } from '@angular/core';
import { GenericAddObjectDialogFormComponent } from '../components/common/generic-add-object-dialog-form/generic-add-object-dialog-form.component';
import { SimpleFormData } from '../models/SimpleFormData.model';
import { Observable, map } from 'rxjs';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';

@Injectable({
  providedIn: 'root',
})
export class CommonEditDialogService {
  constructor(private dialog: MatDialog) { }

  openForm(formData: SimpleFormData): Observable<any> {
    const config = new MatDialogConfig();
    config.disableClose = true;
    config.autoFocus = true;
    config.data = formData;
    config.minWidth = "50vw";
    return this.dialog
      .open(GenericAddObjectDialogFormComponent, config)
      .afterClosed();
  }

  openDialog<T>(
    formData: SimpleFormData,
    cb: Function
  ): Observable<any> {
    return this.openForm(formData).pipe(
      map((res) => {
        if (res instanceof SimpleFormData) {
          const temp = new Object() as T;
          res.fillObject(temp as unknown as Object);
          // console.log('openForm piped response', temp);
          cb(temp);
        }
      })
    );
  }
}
