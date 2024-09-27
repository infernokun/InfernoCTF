import { Component } from '@angular/core';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/enums/role.enum';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent {
  public users: User[] = [];
  public busy = false;

  loading$ = this.userService.loading$;

  roles = Object.values(Role);

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) {
    this.busy = true;
    this.authService.loading$.subscribe((loading) => {
      if (!loading) {
        this.userService.getUsers().subscribe((users: User[]) => {
          if (users) {
            this.users = users;
            this.busy = false;
            this.userService.loadingSubject.next(false);
            console.log('users', users);
          }
        });
      }
    });
  }

  public saveUser(user: User) {
    user.editMode = false;
  }

  public deleteUser(userId: number) {
  }

  public editUser(user: User) {
    user.editMode = true;
  }

  public cancelEdit(user: User) {
    user.editMode = false;
  }

  public addUser() {
  }
}
