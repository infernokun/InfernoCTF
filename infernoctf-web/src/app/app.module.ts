import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { CTFCardComponent } from './components/ctf/ctf-card/ctf-card.component';
import { EnvironmentService } from './services/environment.service';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from './material.module';
import { EditDialogComponent } from './components/common/edit-dialog/edit-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ViewDialogComponent } from './components/common/view-dialog/view-dialog.component';
import { AuthInterceptor } from './services/auth/auth-interceptor.service';
import { CTFMainComponent } from './components/ctf/ctf-main/ctf-main.component';
import { RegisterComponent } from './components/register/register.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserComponent } from './components/user/user.component';

export function init_app(environmentService: EnvironmentService) {
  return () => environmentService.load().then(() => {
    console.log('App initialized');
  });
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    CTFCardComponent,
    EditDialogComponent,
    ViewDialogComponent,
    CTFMainComponent,
    RegisterComponent,
    UserComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    MaterialModule
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: init_app,
      deps: [EnvironmentService],
      multi: true,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
