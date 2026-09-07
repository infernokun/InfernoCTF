import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-ctf-main',
  templateUrl: './ctf-main.component.html',
  styleUrl: './ctf-main.component.scss',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CTFMainComponent {
  // inject() so the field initializer below can read it.
  private readonly authService = inject(AuthService);

  readonly loading = this.authService.loading;
}
