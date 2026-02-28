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
 *
 * @Input alojamiento     - Objeto Alojamiento con los datos a mostrar
 * @Input calificacion    - Promedio de calificación (0-5). Opcional, default 0
 * @Input totalResenas    - Número total de reseñas. Opcional, default 0
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

  /** Imagen placeholder cuando el alojamiento no tiene imagenPrincipal */
  readonly placeholderImg = 'https://placehold.co/400x260/e2e8f0/94a3b8?text=Sin+imagen';

  constructor(private router: Router) {}

  /** Navega al detalle del alojamiento */
  verDetalle(): void {
    if (this.alojamiento?.id) {
      this.router.navigate(['/alojamientos', this.alojamiento.id]);
    }
  }

  /**
   * Formatea el precio con separador de miles colombiano.
   * Ej: 150000 → "150.000"
   */
  get precioFormateado(): string {
    return this.alojamiento?.precioPorNoche
      ? this.alojamiento.precioPorNoche.toLocaleString('es-CO')
      : '0';
  }

  /**
   * Genera array de 5 elementos para renderizar las estrellas.
   * Cada elemento es 'full', 'half' o 'empty' según la calificación.
   */
  get estrellas(): Array<'full' | 'half' | 'empty'> {
    return Array.from({ length: 5 }, (_, i) => {
      const pos = i + 1;
      if (this.calificacion >= pos) return 'full';
      if (this.calificacion >= pos - 0.5) return 'half';
      return 'empty';
    });
  }

  /** Muestra la calificación con un decimal o "Nuevo" si no tiene reseñas */
  get calificacionLabel(): string {
    return this.totalResenas > 0
      ? this.calificacion.toFixed(1)
      : 'Nuevo';
  }

  /** Texto de reseñas: "32 reseñas" o "Sin reseñas" */
  get resenasLabel(): string {
    if (this.totalResenas === 0) return 'Sin reseñas';
    if (this.totalResenas === 1) return '1 reseña';
    return `${this.totalResenas} reseñas`;
  }
}