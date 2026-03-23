import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { AlojamientoServicio} from '../app/models/alojamiento-servicio.model';
import { ServicioDisponible } from '../app/models/servicio.model';

/**
 * AlojamientoServicioService — ALOJ-5 / ALOJ-10 / ALOJ-19
 *
 * Gestiona las llamadas al backend para los servicios de un alojamiento.
 *
 * Endpoints consumidos:
 * - GET    /api/servicios                                             → listar servicios disponibles (ALOJ-10)
 * - GET    /api/alojamientos-servicios/alojamiento/:id/servicios     → servicios de un alojamiento
 * - GET    /api/alojamientos-servicios/servicio/:id/alojamientos     → alojamientos con un servicio (ALOJ-19)
 * - POST   /api/alojamientos-servicios                               → asociar servicio a alojamiento (ALOJ-10)
 * - DELETE /api/alojamientos-servicios/alojamiento/:aId/servicio/:sId → desasociar (ALOJ-10)
 */
@Injectable({
  providedIn: 'root'
})
export class AlojamientoServicioService {

  private readonly apiUrl        = `${environment.apiUrl}/alojamientos-servicios`;
  private readonly serviciosUrl  = `${environment.apiUrl}/servicios`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-10: LISTAR TODOS LOS SERVICIOS DISPONIBLES (para checkboxes)
  // ─────────────────────────────────────────────────────────────────

  getServiciosDisponibles(): Observable<ServicioDisponible[]> {
    return this.http
      .get<ServicioDisponible[]>(this.serviciosUrl)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener los servicios disponibles');
          return throwError(() => new Error(mensaje));
        })
      );
  }

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

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-19: LISTAR ALOJAMIENTOS QUE TIENEN UN SERVICIO (para filtro)
  // ─────────────────────────────────────────────────────────────────

  getAlojamientosByServicio(servicioId: number): Observable<AlojamientoServicio[]> {
    return this.http
      .get<AlojamientoServicio[]>(`${this.apiUrl}/servicio/${servicioId}/alojamientos`)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener alojamientos por servicio');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-10: ASOCIAR SERVICIO A ALOJAMIENTO
  // ─────────────────────────────────────────────────────────────────

  addServicio(lodgingId: number, serviceId: number): Observable<AlojamientoServicio> {
    return this.http
      .post<AlojamientoServicio>(this.apiUrl, { lodgingId, serviceId })
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al asociar el servicio al alojamiento');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-10: DESASOCIAR SERVICIO DE ALOJAMIENTO
  // ─────────────────────────────────────────────────────────────────

  removeServicio(alojamientoId: number, servicioId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/alojamiento/${alojamientoId}/servicio/${servicioId}`)
      .pipe(
        catchError(error => {
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al desasociar el servicio');
          return throwError(() => new Error(mensaje));
        })
      );
  }
}
