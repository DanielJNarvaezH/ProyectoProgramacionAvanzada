/**
 * Interfaz Comentario — alineada con ComentarioDTO del backend.
 *
 * Campos según la API:
 * reservationId, userId, rating (1-5), text
 *
 * El backend devuelve además id y fecha en algunos endpoints.
 */
export interface Comentario {
  id?: number;
  reservationId: number;
  userId: number;
  rating: number;        // 1-5
  text: string;
  fecha?: string;        // ISO string cuando el backend lo incluye
}
