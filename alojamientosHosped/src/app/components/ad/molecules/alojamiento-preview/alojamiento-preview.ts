import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Alojamiento } from '../../../../models/alojamiento.model';

/**
 * AlojamientoPreviewComponent — ALOJ-14
 *
 * Muestra cómo se verá el alojamiento públicamente antes de guardar.
 * Recibe los datos del formulario como @Input y los renderiza
 * con la misma estructura visual que el detalle público.
 *
 * Uso:
 *   <app-alojamiento-preview [alojamiento]="datosFormulario"></app-alojamiento-preview>
 */
@Component({
  selector: 'app-alojamiento-preview',
  standalone: false,
  templateUrl: './alojamiento-preview.html',
  styleUrls: ['./alojamiento-preview.scss']
})
export class AlojamientoPreviewComponent implements OnChanges {

  @Input() alojamiento: Partial<Alojamiento> | null = null;

  mapaUrl = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['alojamiento']) {
      this.construirMapaUrl();
    }
  }

  private construirMapaUrl(): void {
    const lat = this.alojamiento?.latitude;
    const lng = this.alojamiento?.longitude;
    if (lat && lng) {
      this.mapaUrl =
        `https://www.openstreetmap.org/export/embed.html` +
        `?bbox=${lng - 0.01},${lat - 0.01},${lng + 0.01},${lat + 0.01}` +
        `&layer=mapnik&marker=${lat},${lng}`;
    } else {
      this.mapaUrl = '';
    }
  }

  get precioFormateado(): string {
    return this.alojamiento?.pricePerNight
      ? this.alojamiento.pricePerNight.toLocaleString('es-CO')
      : '0';
  }

  get hayMapa(): boolean {
    return !!this.mapaUrl;
  }

  get tieneImagen(): boolean {
    return !!this.alojamiento?.mainImage;
  }

  get hayDatos(): boolean {
    return !!this.alojamiento?.name;
  }
}
