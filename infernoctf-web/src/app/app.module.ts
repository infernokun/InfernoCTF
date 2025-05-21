import { NgModule, inject, provideAppInitializer } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { CTFCardComponent } from './components/ctf/ctf-card/ctf-card.component';
import { EnvironmentService } from './services/environment.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from './material.module';
import { EditDialogComponent } from './components/common/edit-dialog/edit-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ViewDialogComponent } from './components/common/view-dialog/view-dialog.component';
import { CTFMainComponent } from './components/ctf/ctf-main/ctf-main.component';
import { RegisterComponent } from './components/register/register.component';
import { UserComponent } from './components/user/user.component';
import { CommonDialogComponent } from './components/common/common-dialog/common-dialog.component';
import { GenericDialogQuestionComponent } from './components/common/generic-dialog-question/generic-dialog-question.component';
import { GenericAddObjectDialogFormComponent } from './components/common/generic-add-object-dialog-form/generic-add-object-dialog-form.component';
import { SkeletonDirective } from './directives/skeleton.directive';
import { SkeletonRectComponent } from './components/common/skeleton-rect/skeleton-rect.component';

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
    GenericDialogQuestionComponent,
    GenericAddObjectDialogFormComponent,
    SkeletonDirective,
    SkeletonRectComponent
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
    provideHttpClient(withInterceptorsFromDi())
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }