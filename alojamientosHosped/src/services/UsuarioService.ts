import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { User } from '../app/models';

/**
 * UsuarioService — Gestiona las operaciones de perfil del usuario autenticado.
 *
 * AUTH-21:
 * - getMiPerfil()       → GET  /api/usuarios/me  (leer datos propios)
 * - actualizarPerfil()  → PUT  /api/usuarios/me  (editar datos propios)
 */
@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────
  // GET /api/usuarios/me — Obtener perfil del usuario autenticado
  // ─────────────────────────────────────────────────────────────
  getMiPerfil(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      catchError(error => {
        const mensaje = this.resolverError(error);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────
  // PUT /api/usuarios/me — Actualizar perfil del usuario autenticado
  // ─────────────────────────────────────────────────────────────
  actualizarPerfil(datos: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/me`, datos).pipe(
      catchError(error => {
        const mensaje = this.resolverError(error);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────
  // Manejo de errores
  // ─────────────────────────────────────────────────────────────
  private resolverError(error: any): string {
    if (error.status === 0) {
      return 'No se puede conectar con el servidor.';
    }
    const backendMsg: string = typeof error.error === 'string'
      ? error.error
      : (error.error?.mensaje || error.error?.message || '');

    if (error.status === 401) return 'Sesión expirada. Por favor inicia sesión nuevamente.';
    if (error.status === 404) return 'Usuario no encontrado.';
    if (error.status === 400) return backendMsg || 'Datos inválidos. Revisa el formulario.';
    if (error.status >= 500) return 'Error interno del servidor. Intenta de nuevo más tarde.';
    return backendMsg || 'Error inesperado.';
  }
}
