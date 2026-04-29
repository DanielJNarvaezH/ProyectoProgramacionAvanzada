/**
 * EstadoReserva — RESERV-2
 */
export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA';

/**
 * Reserva — RESERV-2
 * Alineada con ReservaDTO del backend.
 */
export interface Reserva {
  id?:              number;
  guestId:          number;
  lodgingId:        number;
  startDate:        string;   // yyyy-MM-dd
  endDate:          string;   // yyyy-MM-dd
  numGuests:        number;
  totalPrice:       number;
  status:           EstadoReserva;
  cancelDate?:      string;
  cancelReason?:    string;
  /** Fecha y hora en que se creó la reserva — para ordenar por más reciente primero */
  reservationDate?: string;   // ISO datetime — ej: 2026-04-28T22:30:00
}

export type CrearReservaRequest = Omit<Reserva, 'id' | 'cancelDate' | 'cancelReason' | 'reservationDate'>;

export interface CancelarReservaRequest {
  cancelReason: string;
}

export const ESTADO_RESERVA_LABEL: Record<EstadoReserva, string> = {
  PENDIENTE:  'Pendiente',
  CONFIRMADA: 'Confirmada',
  CANCELADA:  'Cancelada',
  COMPLETADA: 'Completada'
};

export const ESTADO_RESERVA_COLOR: Record<EstadoReserva, string> = {
  PENDIENTE:  'warning',
  CONFIRMADA: 'success',
  CANCELADA:  'danger',
  COMPLETADA: 'info'
};