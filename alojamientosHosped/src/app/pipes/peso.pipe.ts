import { Pipe, PipeTransform } from '@angular/core';

/**
 * PesoPipe
 * Formatea un número como precio en pesos colombianos (es-CO).
 * Centraliza la lógica que estaba duplicada en 4 componentes:
 * alojamiento-card, alojamiento-preview, panel-gestion, alojamiento-detalle.
 *
 * Uso en template:
 *   {{ alojamiento.pricePerNight | peso }}   → "150.000"
 *   {{ precio | peso }}                      → "80.000"
 */
@Pipe({
  name: 'peso',
  standalone: false
})
export class PesoPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value == null || isNaN(value)) return '0';
    return value.toLocaleString('es-CO');
  }
}