import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/AuthService';

/**
 * AuthInterceptor — Interceptor HTTP
 *
 * Agrega automáticamente el token JWT en el header Authorization
 * de todas las peticiones HTTP salientes.
 *
 * Formato: Authorization: Bearer <token>
 *
 * Si no hay token activo (usuario no autenticado), la petición
 * se deja pasar sin modificar.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    // Si no hay token, pasar la petición sin modificar
    if (!token) {
      return next.handle(req);
    }

    // Clonar la petición y agregar el header Authorization
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next.handle(authReq);
  }
}
