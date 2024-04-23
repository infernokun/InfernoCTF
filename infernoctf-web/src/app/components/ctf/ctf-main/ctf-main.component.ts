import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-ctf-main',
  templateUrl: './ctf-main.component.html',
  styleUrl: './ctf-main.component.scss'
})
export class CTFMainComponent {
  authenticated: boolean = false;

  constructor(private authService: AuthService) { }

  loading$ = this.authService.loading$;

  ngOnInit(): void {
    /*this.authService.isAuthenticated().subscribe((authenticated) => {
      this.authenticated = authenticated;
    });*/
  }
}
