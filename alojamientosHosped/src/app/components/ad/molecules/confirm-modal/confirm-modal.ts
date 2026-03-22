import { Component, Input, Output, EventEmitter } from '@angular/core';

/**
 * ConfirmModalComponent — Molécula reutilizable de confirmación
 *
 * Reemplaza el modal duplicado en panel-gestion y alojamiento-detalle.
 *
 * Uso:
 * <app-confirm-modal
 *   [visible]="mostrarModal"
 *   [nombreElemento]="alojamiento.name"
 *   [cargando]="eliminando"
 *   [error]="errorEliminacion"
 *   (confirmar)="confirmarEliminar()"
 *   (cancelar)="cerrarModal()"
 * ></app-confirm-modal>
 */
@Component({
  selector: 'app-confirm-modal',
  standalone: false,
  templateUrl: './confirm-modal.html',
  styleUrls: ['./confirm-modal.scss']
})
export class ConfirmModalComponent {

  @Input() visible         = false;
  @Input() titulo          = '¿Eliminar alojamiento?';
  @Input() mensaje         = 'Estás a punto de eliminar';
  @Input() nombreElemento  = '';
  @Input() mensajeSub      = 'Esta acción no se puede deshacer y solo es posible si no tiene reservas activas.';
  @Input() textoConfirmar  = 'Sí, eliminar';
  @Input() iconoConfirmar  = 'fa-solid fa-trash';
  @Input() textoCargando   = 'Eliminando...';
  @Input() cargando        = false;
  @Input() error           = '';

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar  = new EventEmitter<void>();

  onOverlayClick(): void {
    if (!this.cargando) this.cancelar.emit();
  }
}