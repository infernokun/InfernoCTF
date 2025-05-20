import { Component, ViewChild } from '@angular/core';
import { AuthService, UserPayload } from './services/auth.service';
import { DialogService } from './services/dialog.service';
import { LoginComponent } from './components/login/login.component';
import { map, Observable, of, Subject, takeUntil } from 'rxjs';
import { LoginService } from './services/login.service';
import { User } from './models/user.model';

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

  loadingUser$: Observable<boolean> = of(false);
  loggedInUser$: Observable<User | undefined> | undefined;
  
  appVersion: any;
  bannerDisplayStyle: string = 'green-white';

  loading$ = this.authService.loading$;

  private unsubscribe$ = new Subject<void>();

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
    this.checkAuthentication();
  }

  private checkAuthentication() {
    this.authService
      .isAuthenticated()
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(authenticated => {
        console.log(authenticated ? 'Authenticated' : 'Not authenticated');
        this.authService.setLoading(false);
      });
  }


  openLoginModal(): void {
    this.dialogService.openLoginDialog().subscribe((res: any) => {
    });
  }

  openRegisterModal(): void {
    this.dialogService.openRegisterDialog().subscribe((res: any) => {
    });
  }

  logoutButton(): void {
    this.authService.logout();
  }
}
