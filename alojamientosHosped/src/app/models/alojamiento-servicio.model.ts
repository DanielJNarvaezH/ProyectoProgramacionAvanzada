/**
 * Interfaz AlojamientoServicio — alineada con AlojamientoServicioDTO del backend.
 *
 * Representa la relación entre un alojamiento y un servicio.
 * El campo serviceId permite consultar el detalle del servicio.
 */
export interface AlojamientoServicio {
  id?: number;
  lodgingId: number;
  serviceId: number;
  /** Nombre del servicio (enriquecido en cliente si se obtiene del endpoint de servicios) */
  nombre?: string;
  /** Ícono Font Awesome del servicio */
  icono?: string;
}
