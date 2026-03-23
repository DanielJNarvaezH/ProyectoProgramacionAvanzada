import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Alojamiento } from '../../../../models/alojamiento.model';
import { MapService }  from '../../../../../services/MapService';

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

  constructor(private mapService: MapService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['alojamiento']) {
      this.mapaUrl = this.mapService.buildEmbedUrl(
        this.alojamiento?.latitude,
        this.alojamiento?.longitude
      );
    }
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