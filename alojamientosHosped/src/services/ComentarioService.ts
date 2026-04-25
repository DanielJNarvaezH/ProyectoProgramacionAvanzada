import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError, of } from 'rxjs';
import { environment } from '../environments/environment';
import {
  Comentario,
  CrearComentarioRequest,
  ActualizarComentarioRequest,
  RespuestaComentario,
  CrearRespuestaRequest,
  ActualizarRespuestaRequest
} from '../app/models/comentario.model';

/**
 * ComentarioService — COMENT-1
 *
 * Gestiona las llamadas al backend para comentarios y respuestas
 * de alojamientos.
 *
 * Endpoints de comentarios:
 *   POST   /api/comentarios                          → create()
 *   GET    /api/comentarios/:id                      → getById()
 *   GET    /api/comentarios/alojamiento/:id          → getByAlojamiento()
 *   GET    /api/comentarios/alojamiento/:id/promedio → getPromedio()
 *   PUT    /api/comentarios/:id                      → update()
 *   DELETE /api/comentarios/:id                      → delete()
 *
 * Endpoints de respuestas (anfitrión):
 *   POST   /api/respuestas-comentarios               → respond()
 *   GET    /api/respuestas-comentarios/:id           → getRespuestaById()
 *   GET    /api/respuestas-comentarios/comentario/:id → getRespuestasByComentario()
 *   PUT    /api/respuestas-comentarios/:id           → updateRespuesta()
 *   DELETE /api/respuestas-comentarios/:id           → deleteRespuesta()
 */
@Injectable({
  providedIn: 'root'
})
export class ComentarioService {

  private readonly apiUrl        = `${environment.apiUrl}/comentarios`;
  private readonly respuestasUrl = `${environment.apiUrl}/respuestas-comentarios`;

  constructor(private http: HttpClient) {}

  // ── Comentarios ───────────────────────────────────────────────

  /**
   * Crea un nuevo comentario para un alojamiento.
   * Solo huéspedes con reserva completada pueden comentar (validado en backend).
   */
  create(comentario: CrearComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(this.apiUrl, comentario).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al publicar el comentario');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Obtiene un comentario por su ID.
   */
  getById(id: number): Observable<Comentario> {
    return this.http.get<Comentario>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Comentario con ID ${id} no encontrado`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Lista todos los comentarios de un alojamiento.
   * Retorna array vacío si no hay ninguno (404).
   */
  getByAlojamiento(alojamientoId: number): Observable<Comentario[]> {
    return this.http
      .get<Comentario[]>(`${this.apiUrl}/alojamiento/${alojamientoId}`)
      .pipe(
        catchError(error => {
          if (error.status === 404) {
            return of([] as Comentario[]);
          }
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener los comentarios');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  /**
   * Obtiene el promedio de calificaciones de un alojamiento.
   * Retorna 0 si no hay comentarios (404) o cualquier error.
   */
  getPromedio(alojamientoId: number): Observable<number> {
    return this.http
      .get<number>(`${this.apiUrl}/alojamiento/${alojamientoId}/promedio`)
      .pipe(
        catchError(() => of(0))
      );
  }

  /**
   * Actualiza el texto de un comentario existente.
   */
  update(id: number, request: ActualizarComentarioRequest): Observable<Comentario> {
    return this.http.put<Comentario>(`${this.apiUrl}/${id}`, request).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al actualizar el comentario');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Elimina un comentario por su ID.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al eliminar el comentario');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ── Respuestas del anfitrión ──────────────────────────────────

  /**
   * Crea una respuesta de anfitrión a un comentario.
   */
  respond(respuesta: CrearRespuestaRequest): Observable<RespuestaComentario> {
    return this.http.post<RespuestaComentario>(this.respuestasUrl, respuesta).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al publicar la respuesta');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Obtiene una respuesta por su ID.
   */
  getRespuestaById(id: number): Observable<RespuestaComentario> {
    return this.http.get<RespuestaComentario>(`${this.respuestasUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || `Respuesta con ID ${id} no encontrada`);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Lista todas las respuestas de un comentario.
   * Retorna array vacío si no hay ninguna (404).
   */
  getRespuestasByComentario(comentarioId: number): Observable<RespuestaComentario[]> {
    return this.http
      .get<RespuestaComentario[]>(`${this.respuestasUrl}/comentario/${comentarioId}`)
      .pipe(
        catchError(error => {
          if (error.status === 404) {
            return of([] as RespuestaComentario[]);
          }
          const mensaje = typeof error.error === 'string'
            ? error.error
            : (error.error?.mensaje || 'Error al obtener las respuestas');
          return throwError(() => new Error(mensaje));
        })
      );
  }

  /**
   * Actualiza el texto de una respuesta existente.
   */
  updateRespuesta(id: number, request: ActualizarRespuestaRequest): Observable<RespuestaComentario> {
    return this.http.put<RespuestaComentario>(`${this.respuestasUrl}/${id}`, request).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al actualizar la respuesta');
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Elimina una respuesta por su ID.
   */
  deleteRespuesta(id: number): Observable<void> {
    return this.http.delete<void>(`${this.respuestasUrl}/${id}`).pipe(
      catchError(error => {
        const mensaje = typeof error.error === 'string'
          ? error.error
          : (error.error?.mensaje || 'Error al eliminar la respuesta');
        return throwError(() => new Error(mensaje));
      })
    );
  }
}
