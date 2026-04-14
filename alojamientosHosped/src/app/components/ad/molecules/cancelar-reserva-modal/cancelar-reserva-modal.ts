import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ViewChild, ElementRef } from '@angular/core';

/**
 * CancelarReservaModalComponent — RESERV-10
 *
 * Modal de confirmación de cancelación de reserva.
 * Solicita al usuario un motivo obligatorio antes de proceder.
 *
 * Reglas de negocio que aplica el backend (RN16):
 *   - La cancelación solo es posible con >= 48 horas de anticipación al check-in.
 *   - El motivo es obligatorio (validado también en frontend).
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

  @Output() confirmarCancelar = new EventEmitter<string>();
  @Output() cerrarCancelar    = new EventEmitter<void>();

  @ViewChild('motivoRef') motivoRef!: ElementRef<HTMLTextAreaElement>;

  readonly MAX_CHARS = 300;

  // Estado local del motivo — solo se actualiza desde onInput()
  motivo = '';

  /** Se llama en cada keystroke desde el template */
  onInput(event: Event): void {
    this.motivo = (event.target as HTMLTextAreaElement).value;
  }

  get motivoValido(): boolean {
    return this.motivo.trim().length >= 10;
  }

  get charsRestantes(): number {
    return this.MAX_CHARS - this.motivo.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] &&
        changes['visible'].previousValue === false &&
        changes['visible'].currentValue === true) {
      this.motivo = '';
      setTimeout(() => {
        if (this.motivoRef?.nativeElement) {
          this.motivoRef.nativeElement.value = '';
        }
      }, 0);
    }
  }

  onOverlayClick(): void {
    if (!this.cargando) this.cerrarCancelar.emit();
  }

  onConfirmar(): void {
    if (!this.motivoValido || this.cargando) return;
    this.confirmarCancelar.emit(this.motivo.trim());
  }
}