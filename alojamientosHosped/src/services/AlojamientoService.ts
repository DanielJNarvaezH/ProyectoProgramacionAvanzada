import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Alojamiento } from '../app/models/alojamiento.model';

/**
 * AlojamientoService — Servicio para gestión de alojamientos en la plataforma Hosped.
 *
 * Gestiona:
 * - getAll()        → GET    /api/alojamientos
 * - getById()       → GET    /api/alojamientos/:id
 * - create()        → POST   /api/alojamientos
 * - update()        → PUT    /api/alojamientos/:id
 * - delete()        → DELETE /api/alojamientos/:id
 * - getByAnfitrion() → GET   /api/alojamientos/anfitrion/:id
 * - getByCiudad()   → GET    /api/alojamientos/ciudad/:ciudad
 */
@Injectable({
  providedIn: 'root'
})
export class AlojamientoService {

  private readonly apiUrl = `${environment.apiUrl}/alojamientos`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // GET ALL
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene todos los alojamientos activos de la plataforma.
   */
  getAll(): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(this.apiUrl).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Error al obtener los alojamientos';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GET BY ID
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene un alojamiento específico por su ID.
   * @param id - ID del alojamiento
   */
  getById(id: number): Observable<Alojamiento> {
    return this.http.get<Alojamiento>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || `Error al obtener el alojamiento con ID ${id}`;
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // CREATE
  // ─────────────────────────────────────────────────────────────────

  /**
   * Crea un nuevo alojamiento en la plataforma.
   * @param alojamiento - Datos del alojamiento a crear
   */
  create(alojamiento: Alojamiento): Observable<Alojamiento> {
    return this.http.post<Alojamiento>(this.apiUrl, alojamiento).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Error al crear el alojamiento';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // UPDATE
  // ─────────────────────────────────────────────────────────────────

  /**
   * Actualiza los datos de un alojamiento existente.
   * @param id - ID del alojamiento a actualizar
   * @param alojamiento - Datos actualizados
   */
  update(id: number, alojamiento: Alojamiento): Observable<Alojamiento> {
    return this.http.put<Alojamiento>(`${this.apiUrl}/${id}`, alojamiento).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || `Error al actualizar el alojamiento con ID ${id}`;
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // DELETE
  // ─────────────────────────────────────────────────────────────────

  /**
   * Elimina un alojamiento por su ID.
   * El backend realiza un soft delete (marca como inactivo).
   * @param id - ID del alojamiento a eliminar
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || `Error al eliminar el alojamiento con ID ${id}`;
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // MÉTODOS ADICIONALES
  // ─────────────────────────────────────────────────────────────────

  /**
   * Obtiene todos los alojamientos de un anfitrión específico.
   * @param hostId - ID del anfitrión
   */
  getByAnfitrion(hostId: number): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(`${this.apiUrl}/anfitrion/${hostId}`).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Error al obtener los alojamientos del anfitrión';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Busca alojamientos por ciudad.
   * @param ciudad - Nombre de la ciudad
   */
  getByCiudad(ciudad: string): Observable<Alojamiento[]> {
    return this.http.get<Alojamiento[]>(`${this.apiUrl}/ciudad/${ciudad}`).pipe(
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Error al buscar alojamientos por ciudad';
        return throwError(() => new Error(mensaje));
      })
    );
  }
}
