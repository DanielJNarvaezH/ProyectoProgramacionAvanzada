import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError, map } from 'rxjs';
import { environment } from '../environments/environment';
import { Reserva, CrearReservaRequest } from '../app/models';

/**
 * ReservaService — RESERV-1
 *
 * Gestiona las llamadas al backend para el módulo de reservas:
 *
 * - create()           → POST   /api/reservas
 * - getByUser()        → GET    /api/reservas/huesped/:guestId
 * - getByAlojamiento() → GET    /api/reservas/alojamiento/:lodgingId
 * - getById()          → GET    /api/reservas/:id
 * - cancel()           → DELETE /api/reservas/:id?motivo=...
 */
@Injectable({
  providedIn: 'root'
})
export class ReservaService {

  private readonly apiUrl = `${environment.apiUrl}/reservas`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // CREATE — RF15, HU-019
  // POST /api/reservas
  // ─────────────────────────────────────────────────────────────────

  /**
   * Crea una nueva reserva.
   * El backend valida fechas futuras, mínimo 1 noche, capacidad
   * máxima y solapamiento de fechas.
   *
   * @param reserva Datos de la reserva a crear
   * @returns Observable con la reserva creada (incluye id asignado)
   */
  create(reserva: CrearReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(this.apiUrl, reserva).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al crear la reserva');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY USER — RF23, HU-023
  // GET /api/reservas/huesped/:guestId
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene el historial de reservas de un huésped.
   *
   * @param guestId ID del usuario huésped
   * @returns Observable con la lista de reservas del usuario
   */
  getByUser(guestId: number): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.apiUrl}/huesped/${guestId}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al obtener las reservas del usuario');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY ALOJAMIENTO — RF24, HU-024
  // GET /api/reservas/alojamiento/:lodgingId
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene las reservas de un alojamiento (uso del anfitrión).
   *
   * @param lodgingId ID del alojamiento
   * @returns Observable con la lista de reservas del alojamiento
   */
  getByAlojamiento(lodgingId: number): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.apiUrl}/alojamiento/${lodgingId}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al obtener las reservas del alojamiento');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY ID
  // GET /api/reservas/:id
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene una reserva por su ID.
   *
   * @param id ID de la reserva
   * @returns Observable con los datos de la reserva
   */
  getById(id: number): Observable<Reserva> {
    return this.http.get<Reserva>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Error al obtener la reserva con ID ${id}`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // CANCEL — RF21, HU-021
  // DELETE /api/reservas/:id?motivo=...
  // ─────────────────────────────────────────────────────────────────

  /**
   * Cancela una reserva.
   * El backend valida que la cancelación se haga con al menos
   * 48 horas de anticipación al check-in.
   * Nota: el backend retorna 200 con body string — se usa responseType: 'text'
   * para evitar error de parsing JSON.
   *
   * @param id     ID de la reserva a cancelar
   * @param motivo Motivo de la cancelación (obligatorio)
   * @returns Observable<void> que completa al cancelar correctamente
   */
  cancel(id: number, motivo: string): Observable<void> {
    const params = new HttpParams().set('motivo', motivo);
    return this.http.delete(`${this.apiUrl}/${id}`, { params, responseType: 'text' }).pipe(
      map(() => void 0),
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al cancelar la reserva');
        return throwError(() => new Error(mensaje));
      })
    ) as Observable<void>;
  }
}