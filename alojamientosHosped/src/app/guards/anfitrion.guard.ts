import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/AuthService';

/**
 * anfitrionGuard — ALOJ-7
 *
 * Protege rutas que solo deben ser accesibles por usuarios con rol ANFITRION.
 * Si el usuario no está autenticado → redirige a /login
 * Si está autenticado pero no es anfitrión → redirige a /alojamientos
 */
export const anfitrionGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  console.log('anfitrionGuard ejecutado');
  console.log('isAuthenticated:', authService.isAuthenticated());
  console.log('esAnfitrion:', authService.esAnfitrion());


  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (!authService.esAnfitrion()) {
    router.navigate(['/alojamientos']);
    return false;
  }

  return true;
};

