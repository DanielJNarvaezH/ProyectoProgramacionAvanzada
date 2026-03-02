import { Component, Input } from '@angular/core';
import { Comentario } from '../../../../models/comentario.model';

/**
 * ComentarioCardComponent — Molécula (ALOJ-5)
 *
 * Muestra un comentario individual con:
 * - Avatar generado con las iniciales del userId
 * - Calificación en estrellas
 * - Texto del comentario
 * - Fecha formateada
 *
 * Uso:
 * <app-comentario-card [comentario]="item"></app-comentario-card>
 */
@Component({
  selector: 'app-comentario-card',
  standalone: false,
  templateUrl: './comentario-card.html',
  styleUrls: ['./comentario-card.scss']
})
export class ComentarioCardComponent {

  @Input() comentario!: Comentario;

  /** Genera array de 5 elementos para renderizar estrellas */
  get estrellas(): Array<'full' | 'empty'> {
    return Array.from({ length: 5 }, (_, i) =>
      i < this.comentario.rating ? 'full' : 'empty'
    );
  }

  /** Formatea la fecha ISO a dd/mm/aaaa */
  get fechaFormateada(): string {
    if (!this.comentario.fecha) return '';
    try {
      return new Date(this.comentario.fecha).toLocaleDateString('es-CO', {
        year:  'numeric',
        month: 'long',
        day:   'numeric'
      });
    } catch {
      return '';
    }
  }

  /** Iniciales para el avatar: "U{userId}" */
  get avatarLabel(): string {
    return `U${this.comentario.userId}`;
  }

  /** Color de fondo del avatar basado en el userId */
  get avatarColor(): string {
    const colores = [
      '#9B5CFF', '#7B3FE4', '#C084FC', '#818CF8',
      '#60A5FA', '#34D399', '#FBBF24', '#F87171'
    ];
    return colores[(this.comentario.userId ?? 0) % colores.length];
  }
}
