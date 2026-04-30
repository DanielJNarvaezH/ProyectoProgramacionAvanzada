import { Injectable } from '@angular/core';
import {
  HttpInterceptor, HttpRequest, HttpHandler,
  HttpEvent, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastService } from '../../services/ToastService';

/**
 * ErrorInterceptor — INT-4
 *
 * Interceptor global de errores HTTP.
 * Convierte códigos de estado HTTP en mensajes user-friendly
 * mostrados como toasts, sin exponer información técnica al usuario.
 *
 * Errores manejados:
 *   400 → Datos inválidos
 *   401 → Sesión expirada (manejado por AuthInterceptor, aquí silenciado)
 *   403 → Sin permisos
 *   404 → Recurso no encontrado
 *   409 → Conflicto (ej: ya existe)
 *   422 → Datos no procesables
 *   500 → Error interno del servidor
 *   0   → Sin conexión / servidor caído
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  // Rutas que no deben mostrar toast de error
  private readonly rutasSilenciosas = [
    '/api/comentarios/alojamiento',  // 404 esperado cuando no hay comentarios
    '/api/notificaciones',           // 204 es respuesta normal
    '/api/favoritos',                // errores manejados localmente
  ];

  constructor(private toastService: ToastService) {}

  intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {

        // No mostrar toast si la ruta es silenciosa
        if (this.esSilenciosa(req.url)) {
          return throwError(() => error);
        }

        // 401 — manejado por AuthInterceptor (logout + redirect)
        if (error.status === 401) {
          return throwError(() => error);
        }

        // Error de red / servidor caído
        if (error.status === 0) {
          this.toastService.error(
            'No se pudo conectar al servidor. Verifica tu conexión a internet.'
          );
          return throwError(() => error);
        }

        const mensaje = this.getMensaje(error);
        this.toastService.error(mensaje);
        return throwError(() => error);
      })
    );
  }

  // ── Mensaje user-friendly por código HTTP ─────────────────────

  private getMensaje(error: HttpErrorResponse): string {
    // Intentar usar el mensaje del backend si es legible
    const backendMsg = this.extraerMensajeBackend(error);
    if (backendMsg) return backendMsg;

    switch (error.status) {
      case 400: return 'Los datos enviados no son válidos. Revisa el formulario.';
      case 403: return 'No tienes permisos para realizar esta acción.';
      case 404: return 'El recurso solicitado no fue encontrado.';
      case 409: return 'Ya existe un registro con esos datos.';
      case 422: return 'Los datos no pudieron ser procesados. Verifica la información.';
      case 500: return 'Error interno del servidor. Por favor intenta más tarde.';
      case 503: return 'El servicio no está disponible temporalmente. Intenta más tarde.';
      default:  return `Ocurrió un error inesperado (${error.status}). Intenta de nuevo.`;
    }
  }

  private extraerMensajeBackend(error: HttpErrorResponse): string | null {
    if (!error.error) return null;

    // Mensaje string directo del backend
    if (typeof error.error === 'string' && error.error.length < 200) {
      return error.error;
    }

    // Objeto JSON con campo mensaje/message
    if (typeof error.error === 'object') {
      const msg = error.error.mensaje || error.error.message || error.error.error;
      if (msg && typeof msg === 'string' && msg.length < 200) return msg;
    }

    return null;
  }

  private esSilenciosa(url: string): boolean {
    return this.rutasSilenciosas.some(ruta => url.includes(ruta));
  }
}
