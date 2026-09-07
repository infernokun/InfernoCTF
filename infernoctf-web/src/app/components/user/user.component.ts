import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { Role } from '../../models/enums/role.enum';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserComponent implements OnInit {
  private readonly userService = inject(UserService);

  /** The service owns the list; this is the same signal, not a copy. */
  readonly users = this.userService.users;
  readonly busy = signal(false);

  readonly roles = Object.values(Role);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.busy.set(true);
    this.userService.getAllUsers().subscribe({
      complete: () => this.busy.set(false),
      error: (error) => {
        console.error('Failed to load users:', error);
        this.busy.set(false);
      }
    });
  }

  public saveUser(user: User) {
    user.editMode = false;
  }

  public deleteUser(userId: string) {
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
