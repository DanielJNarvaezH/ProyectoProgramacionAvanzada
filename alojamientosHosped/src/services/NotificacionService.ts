import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError, BehaviorSubject, of, map } from 'rxjs';
import { environment } from '../environments/environment';
import { Notificacion } from '../app/models/notificacion.model';

/**
 * NotificacionService — RESERV-12
 */
@Injectable({
  providedIn: 'root'
})
export class NotificacionService {

  private readonly apiUrl = `${environment.apiUrl}/notificaciones`;

  private noLeidasSubject = new BehaviorSubject<number>(0);
  noLeidas$ = this.noLeidasSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ── Listar ────────────────────────────────────────────────────

  getPorUsuario(usuarioId: number): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.apiUrl}/usuario/${usuarioId}`).pipe(
      map(lista => lista ?? []),
      catchError(() => of([]))
    );
  }

  getNoLeidas(usuarioId: number): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.apiUrl}/usuario/${usuarioId}/no-leidas`).pipe(
      map(lista => lista ?? []),
      catchError(() => of([]))
    );
  }

  // ── Contador ──────────────────────────────────────────────────

  contarNoLeidas(usuarioId: number): Observable<{ noLeidas: number }> {
    return this.http.get<{ noLeidas: number }>(
      `${this.apiUrl}/usuario/${usuarioId}/contar-no-leidas`
    ).pipe(catchError(() => of({ noLeidas: 0 })));
  }

  actualizarContador(usuarioId: number): void {
    this.contarNoLeidas(usuarioId).subscribe(res => {
      this.noLeidasSubject.next(res.noLeidas ?? 0);
    });
  }

  actualizarBadge(cantidad: number): void {
    this.noLeidasSubject.next(cantidad);
  }

  // ── Marcar leída ──────────────────────────────────────────────

  marcarLeida(id: number): Observable<Notificacion> {
    return this.http.put<Notificacion>(`${this.apiUrl}/${id}/leer`, {}).pipe(
      catchError(error => throwError(() => new Error(error.error || 'Error al marcar como leída')))
    );
  }

  marcarTodasLeidas(usuarioId: number): Observable<string> {
    return this.http.put<string>(
      `${this.apiUrl}/usuario/${usuarioId}/leer-todas`, {}
    ).pipe(
      catchError(error => throwError(() => new Error(error.error || 'Error')))
    );
  }

  // ── Eliminar ──────────────────────────────────────────────────

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => throwError(() => new Error(error.error || 'Error al eliminar')))
    );
  }

  eliminarLeidas(usuarioId: number): Observable<string> {
    return this.http.delete<string>(
      `${this.apiUrl}/usuario/${usuarioId}/leidas`
    ).pipe(
      catchError(error => throwError(() => new Error(error.error || 'Error')))
    );
  }
}