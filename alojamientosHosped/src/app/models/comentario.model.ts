/**
 * comentario.model.ts — COMENT-2
 *
 * Interfaces alineadas con los DTOs del backend:
 *   ComentarioDTO          → Comentario
 *   RespuestaComentarioDTO → RespuestaComentario
 *
 * Mapeo de campos según ComentarioDataMapper:
 *   id            ← comentario.id   (PK — necesario para COMENT-6 respuestas)
 *   reservationId ← reserva.id
 *   userId        ← usuario.id
 *   rating        ← calificacion    (1-5)
 *   text          ← texto           (max 500)
 */

// ── Comentario ────────────────────────────────────────────────────────────────

export interface Comentario {
  id?:           number;   // PK del comentario — usado por RespuestaComentarioComponent
  reservationId: number;
  userId:        number;
  rating:        number;   // 1-5
  text:          string;   // max 500 caracteres
  fecha?:        string;   // ISO string
}

export type CrearComentarioRequest = Omit<Comentario, 'id'>;

export interface ActualizarComentarioRequest {
  text: string;
}

// ── RespuestaComentario ───────────────────────────────────────────────────────

export interface RespuestaComentario {
  commentId: number;
  hostId:    number;
  text:      string;
}

export type CrearRespuestaRequest = RespuestaComentario;

export interface ActualizarRespuestaRequest {
  text: string;
}