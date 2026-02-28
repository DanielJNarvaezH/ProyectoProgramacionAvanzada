import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { AuthService } from '../../services/AuthService';
import { Router } from '@angular/router';

/**
 * AuthInterceptor — Interceptor HTTP
 *
 * Responsabilidades:
 * 1. Agrega automáticamente el token JWT en el header Authorization.
 * 2. Excluye las rutas públicas (/auth/login, /auth/register, etc.)
 * 3. Si el backend responde 401, redirige al login.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // Rutas que no requieren token
  private readonly rutasPublicas = [
    '/auth/login',
    '/auth/register',
    '/auth/recuperar-contrasena',
    '/auth/reset-contrasena'
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    // Si no hay token o es una ruta pública, pasar sin modificar
    const esRutaPublica = this.rutasPublicas.some(ruta => req.url.includes(ruta));
    if (!token || esRutaPublica) {
      return next.handle(req);
    }

    // Agregar token a la petición
    return next.handle(this.agregarToken(req, token)).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }

  /**
   * Clona la request agregando el header Authorization con el token.
   */
  private agregarToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
}
