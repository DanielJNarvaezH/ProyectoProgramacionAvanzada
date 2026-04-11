import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Alojamiento } from '../../../../models';

/**
 * ReservaResumenModalComponent — RESERV-7
 *
 * Modal de confirmación de reserva. Muestra el resumen completo
 * de los datos antes de ejecutar la llamada al API.
 *
 * Inputs:
 *   visible       — controla si el modal está abierto
 *   alojamiento   — datos del alojamiento seleccionado
 *   startDate     — fecha de inicio (yyyy-MM-dd)
 *   endDate       — fecha de fin    (yyyy-MM-dd)
 *   noches        — número de noches calculado
 *   numGuests     — número de huéspedes
 *   precioTotal   — precio total calculado
 *   cargando      — spinner mientras se procesa la API
 *   error         — mensaje de error devuelto por el API
 *
 * Outputs:
 *   confirmar  — el usuario presionó "Confirmar reserva"
 *   cancelar   — el usuario cerró el modal sin confirmar
 *
 * Uso:
 * <app-reserva-resumen-modal
 *   [visible]="mostrarModalReserva"
 *   [alojamiento]="alojamiento"
 *   [startDate]="rangoReserva.startDate"
 *   [endDate]="rangoReserva.endDate"
 *   [noches]="noches"
 *   [numGuests]="numGuests"
 *   [precioTotal]="precioTotal"
 *   [cargando]="enviandoReserva"
 *   [error]="errorReserva"
 *   (confirmar)="confirmarReserva()"
 *   (cancelar)="cerrarModalReserva()"
 * ></app-reserva-resumen-modal>
 */
@Component({
  selector: 'app-reserva-resumen-modal',
  standalone: false,
  templateUrl: './reserva-resumen-modal.html',
  styleUrls: ['./reserva-resumen-modal.scss']
})
export class ReservaResumenModalComponent {

  @Input() visible      = false;
  @Input() alojamiento: Alojamiento | null = null;
  @Input() startDate    = '';
  @Input() endDate      = '';
  @Input() noches       = 0;
  @Input() numGuests    = 1;
  @Input() precioTotal  = 0;
  @Input() cargando     = false;
  @Input() error        = '';

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar  = new EventEmitter<void>();

  /** Cierra el modal al hacer clic en el overlay, salvo que esté procesando */
  onOverlayClick(): void {
    if (!this.cargando) this.cancelar.emit();
  }

  /** Formatea una fecha yyyy-MM-dd al formato legible en español */
  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha + 'T00:00:00').toLocaleDateString('es-CO', {
      weekday: 'short',
      day:     'numeric',
      month:   'long',
      year:    'numeric'
    });
  }

  /** Precio por noche formateado */
  get precioPorNoche(): number {
    return this.alojamiento?.pricePerNight ?? 0;
  }
}
