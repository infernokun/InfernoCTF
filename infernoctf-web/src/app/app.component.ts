import { Component, inject } from '@angular/core';
import { AuthService, UserPayload } from './services/auth.service';
import { DialogService } from './services/dialog.service';
import { LoginComponent } from './components/login/login.component';
import { Subject, takeUntil } from 'rxjs';
import { LoginService } from './services/login.service';
import { User } from './models/user.model';

declare var require: any;
const { version: appVersion } = require('../../package.json');
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: false
})
export class AppComponent {
  title = 'InfernoCTF';

  header: string = 'UNCLASSIFIED';
  footer: string = 'UNCLASSIFIED';

  // inject(), not constructor parameters: the field initializers below read these, and
  // parameter properties are not assigned yet at that point.
  private readonly authService = inject(AuthService);
  private readonly dialogService = inject(DialogService);

  readonly loggedInUser = this.authService.user;
  readonly loadingUser = this.authService.loading;
  
  appVersion: any;
  bannerDisplayStyle: string = 'green-white';

  private unsubscribe$ = new Subject<void>();

  constructor() {
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

  openProfileModal() { }

  openUserSettingsModal() { }
}
