import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Imagen } from '../app/models/imagen.model';

/**
 * ImagenService — ALOJ-5
 *
 * Gestiona las llamadas al backend para las imágenes de alojamientos.
 *
 * Endpoints consumidos:
 * - GET /api/imagenes/alojamiento/:id → lista de imágenes ordenadas
 */
@Injectable({
  providedIn: 'root'
})
export class ImagenService {

  private readonly apiUrl = `${environment.apiUrl}/imagenes`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // LISTAR IMÁGENES DE UN ALOJAMIENTO
  // ─────────────────────────────────────────────────────────────────

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
}
