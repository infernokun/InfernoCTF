import { ComponentType } from '@angular/cdk/overlay';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { LoginComponent } from '../components/login/login.component';
import { RegisterComponent } from '../components/register/register.component';
import { SimpleFormData } from '../models/SimpleFormData.model';
import { StoredObject } from '../models/stored-object.model';
import { AddDialogFormComponent } from '../components/common/add-dialog-form/add-dialog-form.component';

@Injectable({
  providedIn: 'root'
})
export class CommonEditDialogService {
  constructor(private dialog: MatDialog) { }

  openForm(formData: SimpleFormData | undefined, component?: ComponentType<any>, customConfig?: MatDialogConfig): Observable<any> {
    const config = customConfig || new MatDialogConfig();
    config.disableClose = config.disableClose ?? true;
    config.autoFocus = config.autoFocus ?? true;
    config.data = formData ?? config.data;
    config.minWidth = config.minWidth ?? "50vw";
    return this.dialog
      .open(component ? component : AddDialogFormComponent, config)
      .afterClosed();
  }

  openDialog<T extends StoredObject>(
    formData: SimpleFormData,
    cb: Function
  ): Observable<any> {
    return this.openForm(formData).pipe(
      map((res) => {
        if (res instanceof SimpleFormData) {
          const temp = new Object() as T;
          res.fillObject(temp);
          cb(temp);
        }
      })
    );
  }

  openLoginDialog(): Observable<any> {
    const config = new MatDialogConfig();
    config.disableClose = false;
    return this.openForm(undefined, LoginComponent, config);
  }

  openRegisterDialog(): Observable<any> {
    const config = new MatDialogConfig();
    config.disableClose = false;
    return this.openForm(undefined, RegisterComponent, config);
  }
}
