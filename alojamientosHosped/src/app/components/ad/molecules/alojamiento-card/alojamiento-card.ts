import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { Alojamiento } from '../../../../models';

/**
 * AlojamientoCardComponent — Molécula reutilizable (ALOJ-3)
 *
 * Card que muestra la información resumida de un alojamiento:
 * foto principal, nombre, ciudad, precio por noche y calificación.
 *
 * Uso:
 * <app-alojamiento-card
 *   [alojamiento]="item"
 *   [calificacion]="4.8"
 *   [totalResenas]="32">
 * </app-alojamiento-card>
 */
@Component({
  selector: 'app-alojamiento-card',
  standalone: false,
  templateUrl: './alojamiento-card.html',
  styleUrls: ['./alojamiento-card.scss']
})
export class AlojamientoCardComponent {

  @Input() alojamiento!: Alojamiento;
  @Input() calificacion: number = 0;
  @Input() totalResenas: number = 0;

  readonly placeholderImg = 'https://placehold.co/400x260/e2e8f0/94a3b8?text=Sin+imagen';

  constructor(private router: Router) {}

  verDetalle(): void {
    if (this.alojamiento?.id) {
      this.router.navigate(['/alojamientos', this.alojamiento.id]);
    }
  }

  /** Formatea el precio con separador de miles colombiano. Ej: 180000 → "180.000" */
  get precioFormateado(): string {
    return this.alojamiento?.pricePerNight
      ? this.alojamiento.pricePerNight.toLocaleString('es-CO')
      : '0';
  }

  /** Genera array de 5 elementos: 'full' | 'half' | 'empty' según la calificación */
  get estrellas(): Array<'full' | 'half' | 'empty'> {
    return Array.from({ length: 5 }, (_, i) => {
      const pos = i + 1;
      if (this.calificacion >= pos) return 'full';
      if (this.calificacion >= pos - 0.5) return 'half';
      return 'empty';
    });
  }

  get calificacionLabel(): string {
    return this.totalResenas > 0
      ? this.calificacion.toFixed(1)
      : 'Nuevo';
  }

  get resenasLabel(): string {
    if (this.totalResenas === 0) return 'Sin reseñas';
    if (this.totalResenas === 1) return '1 reseña';
    return `${this.totalResenas} reseñas`;
  }
}