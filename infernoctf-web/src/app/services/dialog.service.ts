import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { CTFEntity } from '../models/ctf-entity.model';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EditDialogComponent } from '../components/common/edit-dialog/edit-dialog.component';
import { ViewDialogComponent } from '../components/common/view-dialog/view-dialog.component';
import { ComponentType } from '@angular/cdk/portal';
import { LoginComponent } from '../components/login/login.component';
import { RegisterComponent } from '../components/register/register.component';

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  constructor(private dialog: MatDialog) { }

  openForm(formData: CTFEntity | undefined, component: ComponentType<unknown>): Observable<any> {
    const config = new MatDialogConfig();
    config.disableClose = false;
    config.autoFocus = true;
    config.data = formData;
    config.minWidth = "50vw";
    return this.dialog
      .open(component, config)
      .afterClosed();
  }

  openViewDialog(formData: CTFEntity): Observable<any> {
    return this.openForm(formData, ViewDialogComponent);
  }

  openEditDialog(formData: CTFEntity): Observable<any> {
    return this.openForm(formData, EditDialogComponent);
  }

  openLoginDialog(): Observable<any> {
    return this.openForm(undefined, LoginComponent);
  }

  openRegisterDialog(): Observable<any> {
    return this.openForm(undefined, RegisterComponent);
  }
}
