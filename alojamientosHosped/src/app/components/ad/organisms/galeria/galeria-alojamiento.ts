import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Imagen } from '../../../../models/imagen.model';

/**
 * GaleriaAlojamientoComponent — Organismo (ALOJ-5)
 *
 * Galería de imágenes del alojamiento:
 * - Imagen principal grande a la izquierda
 * - Hasta 4 miniaturas a la derecha (responsive)
 * - Lightbox básico al hacer clic en cualquier imagen
 * - Fallback a imagen placeholder si no hay fotos
 *
 * Uso:
 * <app-galeria-alojamiento
 *   [imagenes]="lista"
 *   [mainImage]="alojamiento.mainImage">
 * </app-galeria-alojamiento>
 */
@Component({
  selector: 'app-galeria-alojamiento',
  standalone: false,
  templateUrl: './galeria-alojamiento.html',
  styleUrls: ['./galeria-alojamiento.scss']
})
export class GaleriaAlojamientoComponent implements OnChanges {

  @Input() imagenes: Imagen[]  = [];
  @Input() mainImage?: string;
  @Input() nombre = '';

  readonly placeholder = 'https://placehold.co/800x500/e2e8f0/94a3b8?text=Sin+imagen';

  /** Imagen activa en el lightbox */
  lightboxUrl: string | null = null;

  /** Imagen principal de la galería (primera o mainImage del alojamiento) */
  imagenPrincipal = '';

  /** Hasta 4 miniaturas adicionales */
  miniaturas: Imagen[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['imagenes'] || changes['mainImage']) {
      this.construirGaleria();
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // Construcción de galería
  // ─────────────────────────────────────────────────────────────────

  private construirGaleria(): void {
    const imgs = this.imagenes ?? [];

    if (imgs.length > 0) {
      // Respetar el campo mainImage del alojamiento:
      // buscar la imagen que coincide con mainImage y ponerla como principal
      const idxPrincipal = this.mainImage
        ? imgs.findIndex(img => img.url === this.mainImage)
        : -1;

      if (idxPrincipal > 0) {
        // mainImage existe en la lista pero no es la primera — reordenar
        const principal = imgs[idxPrincipal];
        const resto     = imgs.filter((_, i) => i !== idxPrincipal);
        this.imagenPrincipal = principal.url;
        this.miniaturas      = resto.slice(0, 4);
      } else if (idxPrincipal === 0) {
        // mainImage ya es la primera — comportamiento normal
        this.imagenPrincipal = imgs[0].url;
        this.miniaturas      = imgs.slice(1, 5);
      } else {
        // mainImage no coincide con ninguna imagen de la tabla
        // (caso: se cambió mainImage pero la tabla imagen no se actualizó)
        // usar mainImage directamente como principal y todas las de tabla como miniaturas
        this.imagenPrincipal = this.mainImage || imgs[0].url;
        this.miniaturas      = imgs.slice(0, 4);
      }
    } else {
      // Sin imágenes en tabla imagen — usar mainImage del alojamiento
      this.imagenPrincipal = this.mainImage || this.placeholder;
      this.miniaturas      = [];
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // Lightbox
  // ─────────────────────────────────────────────────────────────────

  abrirLightbox(url: string): void {
    this.lightboxUrl = url;
    document.body.style.overflow = 'hidden';
  }

  cerrarLightbox(): void {
    this.lightboxUrl = null;
    document.body.style.overflow = '';
  }

  /** Navega entre imágenes en el lightbox */
  navegarLightbox(direccion: 1 | -1): void {
    const todasLasUrls = this.todasLasImagenes;
    const idx = todasLasUrls.indexOf(this.lightboxUrl ?? '');
    if (idx === -1) return;
    const siguiente = (idx + direccion + todasLasUrls.length) % todasLasUrls.length;
    this.lightboxUrl = todasLasUrls[siguiente];
  }

  get todasLasImagenes(): string[] {
    const imgs = this.imagenes ?? [];
    if (imgs.length > 0) {
      return imgs.map(i => i.url);
    }
    return [this.imagenPrincipal];
  }

  get hayMasImagenes(): boolean {
    return (this.imagenes ?? []).length > 5;
  }

  get cantidadRestante(): number {
    return (this.imagenes ?? []).length - 5;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholder;
  }
}