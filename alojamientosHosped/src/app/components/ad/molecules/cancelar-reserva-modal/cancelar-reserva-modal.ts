import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

/**
 * CancelarReservaModalComponent — RESERV-10
 *
 * Modal de confirmación de cancelación de reserva.
 * Solicita al usuario un motivo obligatorio antes de proceder.
 *
 * Reglas de negocio que aplica el backend (RN16):
 *   - La cancelación solo es posible con >= 48 horas de anticipación al check-in.
 *   - El motivo es obligatorio (validado también en frontend).
 *
 * Inputs:
 *   visible            — controla visibilidad del modal
 *   nombreAlojamiento  — nombre del alojamiento para mostrar en el modal
 *   fechaCheckIn       — fecha de check-in formateada (informativa)
 *   cargando           — spinner mientras se procesa la petición
 *   error              — mensaje de error devuelto por el API
 *
 * Outputs:
 *   confirmar  — emite el motivo ingresado para ejecutar la cancelación
 *   cancelar   — el usuario cerró el modal sin confirmar
 *
 * Uso:
 * <app-cancelar-reserva-modal
 *   [visible]="mostrarModalCancelar"
 *   [nombreAlojamiento]="getNombreAlojamiento(reservaSeleccionada.lodgingId)"
 *   [fechaCheckIn]="formatearFecha(reservaSeleccionada.startDate)"
 *   [cargando]="cancelando"
 *   [error]="errorCancelacion"
 *   (confirmar)="confirmarCancelacion($event)"
 *   (cancelar)="cerrarModalCancelar()"
 * ></app-cancelar-reserva-modal>
 */
@Component({
  selector: 'app-cancelar-reserva-modal',
  standalone: false,
  templateUrl: './cancelar-reserva-modal.html',
  styleUrls: ['./cancelar-reserva-modal.scss']
})
export class CancelarReservaModalComponent implements OnChanges {

  @Input() visible           = false;
  @Input() nombreAlojamiento = '';
  @Input() fechaCheckIn      = '';
  @Input() cargando          = false;
  @Input() error             = '';

  @Output() confirmar = new EventEmitter<string>(); // emite el motivo
  @Output() cancelar  = new EventEmitter<void>();

  /** Motivo ingresado por el usuario */
  motivo = '';

  /** Límite de caracteres para el motivo */
  readonly MAX_CHARS = 300;

  /** El formulario es válido si el motivo tiene al menos 10 caracteres */
  get motivoValido(): boolean {
    return this.motivo.trim().length >= 10;
  }

  /** Caracteres restantes */
  get charsRestantes(): number {
    return this.MAX_CHARS - this.motivo.length;
  }

  /** Limpiar el motivo cada vez que el modal se abre */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      this.motivo = '';
    }
  }

  /** Cierra el modal al hacer clic en el overlay, salvo que esté procesando */
  onOverlayClick(): void {
    if (!this.cargando) this.cancelar.emit();
  }

  /** Emite el motivo hacia el componente padre para ejecutar la cancelación */
  onConfirmar(): void {
    if (!this.motivoValido || this.cargando) return;
    this.confirmar.emit(this.motivo.trim());
  }
}
