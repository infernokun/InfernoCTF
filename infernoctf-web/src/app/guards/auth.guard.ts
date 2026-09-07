import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthService } from '../services/auth.service';

/** Allows navigation only for a session the API accepts, refreshing the token if expired. */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(AuthService).ensureAuthenticated().pipe(
    map(authenticated => authenticated || router.createUrlTree(['/'])),
  );
};

/** Routing convenience only; the API enforces the same rule independently. */
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return authService.ensureAuthenticated().pipe(
    map(authenticated =>
      (authenticated && authService.isAdmin()) || router.createUrlTree(['/'])),
  );
};
