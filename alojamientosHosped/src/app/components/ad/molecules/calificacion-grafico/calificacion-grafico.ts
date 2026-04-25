import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Comentario } from '../../../../models/comentario.model';

/**
 * CalificacionGraficoComponent — Molécula (COMENT-10)
 *
 * Gráfico de barras que muestra la distribución porcentual de
 * calificaciones (1-5 estrellas) de un alojamiento.
 *
 * Calcula internamente a partir del array de comentarios:
 *   - Cuántos comentarios tienen cada calificación (1-5)
 *   - El porcentaje de cada una respecto al total
 *   - El ancho de cada barra proporcional al porcentaje
 *
 * Inputs:
 *   @Input() comentarios — Array de comentarios del alojamiento
 *
 * Uso:
 *   <app-calificacion-grafico
 *     [comentarios]="comentarios"
 *   ></app-calificacion-grafico>
 */
@Component({
  selector:    'app-calificacion-grafico',
  standalone:  false,
  templateUrl: './calificacion-grafico.html',
  styleUrls:   ['./calificacion-grafico.scss']
})
export class CalificacionGraficoComponent implements OnChanges {

  @Input() comentarios: Comentario[] = [];

  /** Filas del gráfico de mayor a menor (5→1) */
  filas: FilaGrafico[] = [];

  /** Promedio calculado localmente */
  promedio = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['comentarios']) {
      this.calcular();
    }
  }

  private calcular(): void {
    const total = this.comentarios.length;

    if (total === 0) {
      this.filas    = [];
      this.promedio = 0;
      return;
    }

    // Contar cuántos comentarios tiene cada calificación
    const conteo: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    let suma = 0;

    for (const c of this.comentarios) {
      const r = c.rating;
      if (r >= 1 && r <= 5) {
        conteo[r]++;
        suma += r;
      }
    }

    this.promedio = suma / total;

    // Construir filas de 5 a 1 (de arriba a abajo)
    this.filas = [5, 4, 3, 2, 1].map(stars => ({
      stars,
      cantidad:   conteo[stars],
      porcentaje: Math.round((conteo[stars] / total) * 100)
    }));
  }

  get promedioLabel(): string {
    return this.promedio > 0 ? this.promedio.toFixed(1) : '0.0';
  }

  get totalLabel(): string {
    const n = this.comentarios.length;
    if (n === 0) return 'Sin reseñas';
    return n === 1 ? '1 reseña' : `${n} reseñas`;
  }
}

interface FilaGrafico {
  stars:      number;
  cantidad:   number;
  porcentaje: number;
}