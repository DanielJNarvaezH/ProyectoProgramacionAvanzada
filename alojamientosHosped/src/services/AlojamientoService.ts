import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Alojamiento } from '../app/models/alojamiento.model';

/**
 * AlojamientoService — Servicio para gestión de alojamientos en la plataforma Hosped.
 *
 * Gestiona:
 * - getAll()          → GET    /api/alojamiento/activos
 * - getById()         → GET    /api/alojamiento/:id
 * - create()          → POST   /api/alojamiento
 * - update()          → PUT    /api/alojamiento/:id
 * - delete()          → DELETE /api/alojamiento/:id
 * - getByAnfitrion()  → GET    /api/alojamiento/anfitrion/:hostId
 * - getByCiudad()     → GET    /api/alojamiento/buscar?ciudad=:ciudad
 */
@Injectable({
  providedIn: 'root'
})
export class AlojamientoService {

  private readonly apiUrl = `${environment.apiUrl}/alojamientos`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // GET ALL ACTIVOS
  // ─────────────────────────────────────────────────────────────────

  getAll(): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(`${this.apiUrl}/activos`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al obtener los alojamientos');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY ID
  // ─────────────────────────────────────────────────────────────────

  getById(id: number): Observable<Alojamiento> {
    return this.http.get<Alojamiento>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Error al obtener el alojamiento con ID ${id}`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // CREATE
  // ─────────────────────────────────────────────────────────────────

  create(alojamiento: Alojamiento): Observable<Alojamiento> {
    return this.http.post<Alojamiento>(this.apiUrl, alojamiento).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al crear el alojamiento');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // UPDATE
  // ─────────────────────────────────────────────────────────────────

  update(id: number, alojamiento: Alojamiento): Observable<Alojamiento> {
    return this.http.put<Alojamiento>(`${this.apiUrl}/${id}`, alojamiento).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Error al actualizar el alojamiento con ID ${id}`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // DELETE
  // ─────────────────────────────────────────────────────────────────

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Error al eliminar el alojamiento con ID ${id}`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY ANFITRION
  // ─────────────────────────────────────────────────────────────────

  getByAnfitrion(hostId: number): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(`${this.apiUrl}/anfitrion/${hostId}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al obtener los alojamientos del anfitrión');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // BUSCAR POR CIUDAD
  // ─────────────────────────────────────────────────────────────────

  getByCiudad(ciudad: string): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(`${this.apiUrl}/buscar`, {
      params: { ciudad }
    }).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al buscar alojamientos por ciudad');
        return throwError(() => new Error(mensaje));
      })
    );
  }
}