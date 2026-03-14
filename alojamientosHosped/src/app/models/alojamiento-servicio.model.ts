/**
 * AlojamientoServicio — alineada con AlojamientoServicioDTO del backend.
 *
 * Representa la relación entre un alojamiento y un servicio.
 * serviceName e serviceIcon vienen enriquecidos directamente del backend.
 */
export interface AlojamientoServicio {
  id?: number;
  lodgingId: number;
  serviceId: number;
  serviceName?: string;
  serviceIcon?: string;
}