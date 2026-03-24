import { Directive, ElementRef, OnInit, OnDestroy, Input, Renderer2 } from '@angular/core';

/**
 * LazyImgDirective — ALOJ-22
 *
 * Directiva estructural para carga lazy de imágenes usando IntersectionObserver.
 * Complementa el atributo nativo loading="lazy" con:
 *   - Fade-in suave al entrar al viewport
 *   - Placeholder de color mientras carga
 *   - Soporte para fallback en browsers sin IntersectionObserver
 *
 * Uso en template:
 *   <img appLazyImg [src]="url" alt="..." />
 *
 * Diferencia con loading="lazy" nativo:
 *   - loading="lazy": el browser decide cuándo cargar (sin efecto visual)
 *   - appLazyImg: añade fade-in visible + control preciso del threshold
 */
@Directive({
  selector: 'img[appLazyImg]',
  standalone: false
})
export class LazyImgDirective implements OnInit, OnDestroy {

  /** URL real de la imagen — reemplaza al [src] estándar */
  @Input() appLazyImg = '';

  /** Margen antes de entrar al viewport para precargar (default: 100px) */
  @Input() lazyOffset = '100px';

  private observer: IntersectionObserver | null = null;
  private readonly img: HTMLImageElement;

  constructor(
    private el:       ElementRef<HTMLImageElement>,
    private renderer: Renderer2
  ) {
    this.img = this.el.nativeElement;
  }

  ngOnInit(): void {
    // Estado inicial: invisible hasta que entre al viewport
    this.renderer.setStyle(this.img, 'opacity', '0');
    this.renderer.setStyle(this.img, 'transition', 'opacity 0.4s ease');

    if (!('IntersectionObserver' in window)) {
      // Fallback para browsers sin soporte
      this.cargar();
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.cargar();
            this.observer?.disconnect();
          }
        });
      },
      { rootMargin: this.lazyOffset, threshold: 0 }
    );

    this.observer.observe(this.img);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private cargar(): void {
    if (this.appLazyImg) {
      this.img.src = this.appLazyImg;
    }
    this.img.onload = () => {
      this.renderer.setStyle(this.img, 'opacity', '1');
    };
    // Si la imagen ya estaba cacheada, onload no dispara — mostrar igual
    if (this.img.complete) {
      this.renderer.setStyle(this.img, 'opacity', '1');
    }
  }
}