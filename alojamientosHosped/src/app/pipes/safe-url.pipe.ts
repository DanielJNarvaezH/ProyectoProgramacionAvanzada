import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

/**
 * SafeUrlPipe
 *
 * Permite usar URLs en iframes sin que Angular las bloquee por seguridad.
 * Sólo usar con URLs de origen confiable (en este caso OpenStreetMap embed).
 *
 * Uso en template:
 * [src]="url | safeUrl"
 */
@Pipe({
  name: 'safeUrl',
  standalone: false
})
export class SafeUrlPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {}

  transform(url: string): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
