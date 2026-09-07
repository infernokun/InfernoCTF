import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-ctf-main',
  templateUrl: './ctf-main.component.html',
  styleUrl: './ctf-main.component.scss',
  standalone: false
})
export class CTFMainComponent {
  authenticated: boolean = false;

  loading$: Observable<boolean>;

  constructor(private authService: AuthService) {
    this.loading$ = this.authService.loading$;
  }

  ngOnInit(): void {
    /*this.authService.isAuthenticated().subscribe((authenticated) => {
      this.authenticated = authenticated;
    });*/
  }
}
