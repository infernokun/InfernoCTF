import { NgModule, inject, provideAppInitializer } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { CTFCardComponent } from './components/ctf/ctf-card/ctf-card.component';
import { EnvironmentService } from './services/environment.service';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from './material.module';
import { EditDialogComponent } from './components/common/edit-dialog/edit-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ViewDialogComponent } from './components/common/view-dialog/view-dialog.component';
import { CTFMainComponent } from './components/ctf/ctf-main/ctf-main.component';
import { RegisterComponent } from './components/register/register.component';
import { UserComponent } from './components/user/user.component';
import { CommonDialogComponent } from './components/common/common-dialog/common-dialog.component';
import { SkeletonDirective } from './directives/skeleton.directive';
import { SkeletonRectComponent } from './components/common/skeleton-rect/skeleton-rect.component';
import { AddDialogFormComponent } from './components/common/add-dialog-form/add-dialog-form.component';
import { DialogQuestionComponent } from './components/common/dialog-question/dialog-question.component';
import { DragnDropDirective } from './directives/dragndrop.directive';
import { AuthInterceptor } from './services/auth/auth-interceptor.service';

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
    UserComponent,
    CommonDialogComponent,
    DialogQuestionComponent,
    AddDialogFormComponent,
    SkeletonDirective,
    SkeletonRectComponent,
    DragnDropDirective
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    MaterialModule
  ],
  providers: [
    EnvironmentService,
    provideAppInitializer(() => {
        const initializerFn = (init_app)(inject(EnvironmentService));
        return initializerFn();
      }),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }