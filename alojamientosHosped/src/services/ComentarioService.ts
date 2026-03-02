import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Comentario } from '../app/models/comentario.model';

/**
 * ComentarioService — ALOJ-5
 *
 * Gestiona las llamadas al backend para los comentarios de alojamientos.
 *
 * Endpoints consumidos:
 * - GET  /api/comentarios/alojamiento/:id          → lista de comentarios
 * - GET  /api/comentarios/alojamiento/:id/promedio → promedio de calificaciones
 * - POST /api/comentarios                          → crear comentario
 */
@Injectable({
  providedIn: 'root'
})
export class ComentarioService {

  private readonly apiUrl = `${environment.apiUrl}/comentarios`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // LISTAR POR ALOJAMIENTO
  // ─────────────────────────────────────────────────────────────────

  getByAlojamiento(alojamientoId: number): Observable<Comentario[]> {
    return this.http
      .get<Comentario[]>(`${this.apiUrl}/alojamiento/${alojamientoId}`)
      .pipe(
        catchError(error => {
          // 404 = sin comentarios → devolvemos array vacío como error controlado
          if (error.status === 404) {
            return throwError(() => new Error('SIN_COMENTARIOS'));
          }
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener los comentarios');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ─────────────────────────────────────────────────────────────────
  // PROMEDIO DE CALIFICACIONES
  // ─────────────────────────────────────────────────────────────────

  getPromedio(alojamientoId: number): Observable<number> {
    return this.http
      .get<number>(`${this.apiUrl}/alojamiento/${alojamientoId}/promedio`)
      .pipe(
        catchError(error => {
          if (error.status === 404) {
            return throwError(() => new Error('SIN_CALIFICACIONES'));
          }
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener el promedio');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ─────────────────────────────────────────────────────────────────
  // CREAR COMENTARIO
  // ─────────────────────────────────────────────────────────────────

  crear(comentario: Omit<Comentario, 'id' | 'fecha'>): Observable<Comentario> {
    return this.http
      .post<Comentario>(this.apiUrl, comentario)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al publicar el comentario');
          return throwError(() => new Error(mensaje));
        })
      );
  }
}
