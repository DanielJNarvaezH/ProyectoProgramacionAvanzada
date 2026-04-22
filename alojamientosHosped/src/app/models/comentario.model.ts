/**
 * comentario.model.ts — COMENT-2
 *
 * Interfaces alineadas con los DTOs del backend:
 *   ComentarioDTO        → Comentario
 *   RespuestaComentarioDTO → RespuestaComentario
 *
 * Mapeo de campos según ComentarioDataMapper:
 *   reservationId ← reserva.id
 *   userId        ← usuario.id
 *   rating        ← calificacion  (1-5)
 *   text          ← texto         (max 500)
 */

// ── Comentario ────────────────────────────────────────────────────────────────

/**
 * Comentario — alineado con ComentarioDTO del backend.
 *
 * Endpoints:
 *   POST   /api/comentarios                          → create()
 *   GET    /api/comentarios/:id                      → getById()
 *   GET    /api/comentarios/alojamiento/:id          → listarPorAlojamiento()
 *   GET    /api/comentarios/alojamiento/:id/promedio → getPromedio()
 *   PUT    /api/comentarios/:id                      → actualizar()
 *   DELETE /api/comentarios/:id                      → eliminar()
 */
export interface Comentario {
  reservationId: number;
  userId:        number;
  rating:        number;   // 1-5, @Min(1) @Max(5)
  text:          string;   // max 500 caracteres
  fecha?:        string;   // ISO string — opcional, incluido en algunas respuestas GET

}

/**
 * CrearComentarioRequest — payload para POST /api/comentarios.
 * Igual a Comentario — todos los campos son obligatorios al crear.
 */
export type CrearComentarioRequest = Comentario;

/**
 * ActualizarComentarioRequest — payload para PUT /api/comentarios/:id.
 * El backend solo permite modificar el texto.
 */
export interface ActualizarComentarioRequest {
  text: string;
}

// ── RespuestaComentario ───────────────────────────────────────────────────────

/**
 * RespuestaComentario — alineado con RespuestaComentarioDTO del backend.
 *
 * Endpoints:
 *   POST   /api/respuestas-comentarios                      → create()
 *   GET    /api/respuestas-comentarios/:id                  → getById()
 *   GET    /api/respuestas-comentarios/comentario/:commentId → listarPorComentario()
 *   PUT    /api/respuestas-comentarios/:id                  → actualizar()
 *   DELETE /api/respuestas-comentarios/:id                  → eliminar()
 */
export interface RespuestaComentario {
  commentId: number;   // ID del comentario al que responde
  hostId:    number;   // ID del anfitrión que responde
  text:      string;   // max 500 caracteres
}

/**
 * CrearRespuestaRequest — payload para POST /api/respuestas-comentarios.
 * Igual a RespuestaComentario — todos los campos son obligatorios al crear.
 */
export type CrearRespuestaRequest = RespuestaComentario;

/**
 * ActualizarRespuestaRequest — payload para PUT /api/respuestas-comentarios/:id.
 * El backend solo permite modificar el texto.
 */
export interface ActualizarRespuestaRequest {
  text: string;
}
