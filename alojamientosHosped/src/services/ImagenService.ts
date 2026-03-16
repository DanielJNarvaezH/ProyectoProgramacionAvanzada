import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Imagen } from '../app/models/imagen.model';

/**
 * ImagenService — ALOJ-5 + ALOJ-11
 *
 * Endpoints:
 * - GET    /api/imagenes/alojamiento/:id  → listar imágenes de un alojamiento
 * - POST   /api/imagenes                  → crear imagen (ALOJ-11)
 * - DELETE /api/imagenes/:id              → eliminar imagen (ALOJ-11)
 */
@Injectable({
  providedIn: 'root'
})
export class ImagenService {

  private readonly apiUrl = `${environment.apiUrl}/imagenes`;

  constructor(private http: HttpClient) {}

  // ── GET: listar imágenes de un alojamiento ────────────────────────

  getByAlojamiento(alojamientoId: number): Observable<Imagen[]> {
    return this.http
      .get<Imagen[]>(`${this.apiUrl}/alojamiento/${alojamientoId}`)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener las imágenes');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ── POST: guardar imagen en BD ─────────────────────────────────────

  crear(lodgingId: number, url: string, order: number, description?: string): Observable<Imagen> {
    const body = { lodgingId, url, order, description: description ?? '' };
    return this.http
      .post<Imagen>(this.apiUrl, body)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al guardar la imagen');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ── DELETE: eliminar imagen de BD ──────────────────────────────────

  eliminar(imagenId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${imagenId}`)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al eliminar la imagen');
          return throwError(() => new Error(mensaje));
        })
      );
  }
}