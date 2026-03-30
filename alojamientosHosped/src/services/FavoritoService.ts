import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import { Favorito } from '../app/models';

/**
 * FavoritoService — ALOJ-21
 *
 * Gestiona las llamadas al backend para el sistema de favoritos:
 * - agregar()        → POST   /api/favoritos
 * - eliminar()       → DELETE /api/favoritos/usuario/:uid/alojamiento/:lid
 * - listarPorUsuario → GET    /api/favoritos/usuario/:uid
 * - esFavorito()     → GET    /api/favoritos/usuario/:uid/alojamiento/:lid
 */
@Injectable({
  providedIn: 'root'
})
export class FavoritoService {

  private readonly apiUrl = `${environment.apiUrl}/favoritos`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // AGREGAR FAVORITO
  // ─────────────────────────────────────────────────────────────────

  agregar(userId: number, lodgingId: number): Observable<Favorito> {
    return this.http.post<Favorito>(this.apiUrl, { userId, lodgingId }).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al agregar a favoritos');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // ELIMINAR FAVORITO
  // ─────────────────────────────────────────────────────────────────

  eliminar(userId: number, lodgingId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/usuario/${userId}/alojamiento/${lodgingId}`
    ).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al eliminar de favoritos');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // LISTAR FAVORITOS DE UN USUARIO
  // ─────────────────────────────────────────────────────────────────

  listarPorUsuario(userId: number): Observable<Favorito[]> {
    return this.http.get<Favorito[]>(`${this.apiUrl}/usuario/${userId}`).pipe(
      // El backend siempre devuelve 200 con [] cuando no hay favoritos.
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al cargar los favoritos');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // VERIFICAR SI ES FAVORITO
  // ─────────────────────────────────────────────────────────────────

  esFavorito(userId: number, lodgingId: number): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.apiUrl}/usuario/${userId}/alojamiento/${lodgingId}`
    ).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al verificar favorito');
        return throwError(() => new Error(mensaje));
      })
    );
  }
}