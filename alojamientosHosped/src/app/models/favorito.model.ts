/**
 * Favorito — ALOJ-21
 * Alineado con FavoritoDTO del backend.
 */
export interface Favorito {
  id?: number;
  userId: number;
  lodgingId: number;
  fechaAgregado?: string;
}
