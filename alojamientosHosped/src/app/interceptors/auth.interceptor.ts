import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, switchMap, catchError } from 'rxjs';
import { AuthService } from '../../services/AuthService';
import { Router } from '@angular/router';

/**
 * AuthInterceptor — Interceptor HTTP
 *
 * Responsabilidades (AUTH-18 + AUTH-19):
 * 1. Agrega automáticamente el token JWT en el header Authorization.
 * 2. Excluye las rutas públicas (/auth/login, /auth/register, etc.)
 *    para evitar conflictos cuando hay una sesión activa en localStorage.
 * 3. Si el token está próximo a expirar (< 5 min), lo renueva antes
 *    de enviar la petición usando el refresh token.
 * 4. Si el backend responde 401, intenta renovar el token una vez.
 *    Si falla, redirige al login.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // Rutas que no requieren token aunque haya sesión activa
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

    // Si el token está próximo a expirar y hay refresh token, renovar primero
    if (this.authService.estaProximoAExpirar() && this.authService.getRefreshToken()) {
      return this.authService.refreshAccessToken().pipe(
        switchMap(() => {
          const nuevoToken = this.authService.getToken();
          return next.handle(this.agregarToken(req, nuevoToken!));
        }),
        catchError(() => {
          this.router.navigate(['/login']);
          return throwError(() => new Error('Sesión expirada'));
        })
      );
    }

    // Caso normal: agregar el token actual
    return next.handle(this.agregarToken(req, token)).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si el backend responde 401 y hay refresh token, intentar renovar
        if (error.status === 401 && this.authService.getRefreshToken()) {
          return this.authService.refreshAccessToken().pipe(
            switchMap(() => {
              const nuevoToken = this.authService.getToken();
              return next.handle(this.agregarToken(req, nuevoToken!));
            }),
            catchError(() => {
              this.router.navigate(['/login']);
              return throwError(() => new Error('Sesión expirada'));
            })
          );
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