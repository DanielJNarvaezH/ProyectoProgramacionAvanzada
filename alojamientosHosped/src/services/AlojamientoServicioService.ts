import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { AlojamientoServicio } from '../app/models/alojamiento-servicio.model';

/**
 * AlojamientoServicioService — ALOJ-5
 *
 * Gestiona las llamadas al backend para los servicios de un alojamiento.
 *
 * Endpoints consumidos:
 * - GET /api/alojamientos-servicios/alojamiento/:id/servicios
 */
@Injectable({
  providedIn: 'root'
})
export class AlojamientoServicioService {

  private readonly apiUrl = `${environment.apiUrl}/alojamientos-servicios`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // LISTAR SERVICIOS DE UN ALOJAMIENTO
  // ─────────────────────────────────────────────────────────────────

  getServiciosByAlojamiento(alojamientoId: number): Observable<AlojamientoServicio[]> {
    return this.http
      .get<AlojamientoServicio[]>(`${this.apiUrl}/alojamiento/${alojamientoId}/servicios`)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener los servicios del alojamiento');
          return throwError(() => new Error(mensaje));
        })
      );
  }
}
