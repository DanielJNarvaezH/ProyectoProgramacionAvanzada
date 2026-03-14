/**
 * ServicioDisponible — ALOJ-10
 * Alineada con ServicioDTO del backend (campos en inglés).
 * Usada para cargar los servicios disponibles en el formulario de creación.
 */
export interface ServicioDisponible {
  id: number;
  name: string;
  description?: string;
  icon?: string;
  active: boolean;
}