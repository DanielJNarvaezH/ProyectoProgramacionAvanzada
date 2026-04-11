/**
 * TipoNotificacion — RESERV-12
 * Alineado con el enum Java del backend
 */
export type TipoNotificacion = 'NUEVA_RESERVA' | 'CANCELACION' | 'MENSAJE' | 'PROMOCION' | 'OTRO';

/**
 * Notificacion — RESERV-12
 * Alineada con NotificacionDTO del backend:
 *   userId, type, title, message, read, readDate
 */
export interface Notificacion {
  id?:       number;
  userId:    number;
  type:      string;
  title:     string;
  message:   string;
  read:      boolean;
  readDate?: string;
}

export const TIPO_NOTIFICACION_ICON: Record<string, string> = {
  NUEVA_RESERVA:        'fa-solid fa-calendar-check',
  CANCELACION:          'fa-solid fa-calendar-xmark',
  CANCELACION_RESERVA:  'fa-solid fa-calendar-xmark',
  MENSAJE:              'fa-solid fa-comment',
  NUEVO_COMENTARIO:     'fa-solid fa-comment',
  RESPUESTA_COMENTARIO: 'fa-solid fa-reply',
  PROMOCION:            'fa-solid fa-tag',
  RECORDATORIO:         'fa-solid fa-clock',
  OTRO:                 'fa-solid fa-bell'
};

export const TIPO_NOTIFICACION_COLOR: Record<string, string> = {
  NUEVA_RESERVA:        'success',
  CANCELACION:          'danger',
  CANCELACION_RESERVA:  'danger',
  MENSAJE:              'info',
  NUEVO_COMENTARIO:     'info',
  RESPUESTA_COMENTARIO: 'info',
  PROMOCION:            'warning',
  RECORDATORIO:         'warning',
  OTRO:                 'neutral'
};
