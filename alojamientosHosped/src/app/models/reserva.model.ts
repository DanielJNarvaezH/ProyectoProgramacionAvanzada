/**
 * EstadoReserva — RESERV-2
 *
 * Alineado con ReservaEntity.EstadoReserva del backend.
 * Los valores corresponden exactamente al enum Java:
 * PENDIENTE | CONFIRMADA | CANCELADA | COMPLETADA
 */
export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA';

/**
 * Reserva — RESERV-2
 *
 * Alineada con ReservaDTO del backend (campos en inglés).
 * Mapeo de campos según ReservaDataMapper:
 *   guestId      ← huesped.id
 *   lodgingId    ← alojamiento.id
 *   startDate    ← fechaInicio  (yyyy-MM-dd)
 *   endDate      ← fechaFin     (yyyy-MM-dd)
 *   numGuests    ← numHuespedes
 *   totalPrice   ← precioTotal
 *   status       ← estado       (EstadoReserva)
 *   cancelDate   ← fechaCancelacion (yyyy-MM-dd, nullable)
 *   cancelReason ← motivoCancelacion (nullable)
 *
 * El campo id no está en el DTO pero el backend lo incluye
 * en las respuestas GET — se declara como opcional.
 */
export interface Reserva {
  id?:           number;
  guestId:       number;
  lodgingId:     number;
  startDate:     string;   // yyyy-MM-dd
  endDate:       string;   // yyyy-MM-dd
  numGuests:     number;
  totalPrice:    number;
  status:        EstadoReserva;
  cancelDate?:   string;   // yyyy-MM-dd — solo presente si status === 'CANCELADA'
  cancelReason?: string;   // solo presente si status === 'CANCELADA'
}

/**
 * CrearReservaRequest — payload para POST /api/reservas
 *
 * Omite id, cancelDate y cancelReason que no se envían al crear.
 */
export type CrearReservaRequest = Omit<Reserva, 'id' | 'cancelDate' | 'cancelReason'>;

/**
 * CancelarReservaRequest — payload para cancelar una reserva
 *
 * Solo requiere el motivo; el backend gestiona el cambio de estado y fecha.
 */
export interface CancelarReservaRequest {
  cancelReason: string;
}

/**
 * Helpers de estado — utilidades para trabajar con EstadoReserva en la UI
 */
export const ESTADO_RESERVA_LABEL: Record<EstadoReserva, string> = {
  PENDIENTE:  'Pendiente',
  CONFIRMADA: 'Confirmada',
  CANCELADA:  'Cancelada',
  COMPLETADA: 'Completada'
};

export const ESTADO_RESERVA_COLOR: Record<EstadoReserva, string> = {
  PENDIENTE:  'warning',   // amarillo
  CONFIRMADA: 'success',   // verde
  CANCELADA:  'danger',    // rojo
  COMPLETADA: 'info'       // azul
};