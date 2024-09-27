import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { CTFMainComponent } from './components/ctf/ctf-main/ctf-main.component';
import { authGuard } from './guards/auth.guard';
import { UserComponent } from './components/user/user.component';

const routes: Routes = [
  { path: 'home', component: CTFMainComponent, canActivate: [authGuard] },
  { path: 'room/:room', component: CTFMainComponent, canActivate: [authGuard] },
  { path: 'admin/users', component: UserComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: '', component: HomeComponent }, // Default route
  { path: '**', redirectTo: '' } // Wildcard route for a n404 page, redirect to home
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
