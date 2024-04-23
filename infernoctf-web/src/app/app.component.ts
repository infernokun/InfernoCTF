import { Component, ViewChild } from '@angular/core';
import { AuthService, UserPayload } from './services/auth.service';
import { DialogService } from './services/dialog.service';
import { LoginComponent } from './components/login/login.component';
import { map, Observable, of } from 'rxjs';
import { LoginService } from './services/login.service';

declare var require: any;
const { version: appVersion } = require('../../package.json');
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'InfernoCTF';

  header: string = 'UNCLASSIFIED';
  footer: string = 'UNCLASSIFIED';

  payload$: Observable<UserPayload | undefined> = this.authService.payload$;
  appVersion: any;
  bannerDisplayStyle: string = 'green-white';

  loading$ = this.authService.loading$;

  constructor(
    private authService: AuthService,
    private dialogService: DialogService
  ) {
    this.appVersion = appVersion;
    /*this.authService.loggedInUsername$.subscribe((username) => {
      this.username = username;
    });*/
  }

  ngOnInit(): void {
    this.authService.isAuthenticated().subscribe((authenticated) => {
      if (authenticated) {
        console.log('Authenticated');
      } else {
        console.log('Not authenticated');
      }
      this.authService.loadingSubject.next(false);
    });
  }

  openLoginModal(): void {
    this.dialogService.openLoginDialog().subscribe((res) => {
      console.log('wtf, what', res);
    });
  }

  openRegisterModal(): void {
    this.dialogService.openRegisterDialog().subscribe((res) => {
      console.log('wtf, what', res);
    });
  }

  logoutButton(): void {
    this.authService.logout();
  }
}
