import { Injectable } from '@angular/core';

/**
 * MapService — Servicio utilitario para operaciones de mapa.
 *
 * Centraliza la construcción de URLs de OpenStreetMap embed,
 * lógica que estaba duplicada en alojamiento-preview y alojamiento-detalle.
 * Preparado para Sprint 4 (búsqueda avanzada con mapas).
 *
 * Uso:
 *   this.mapaUrl = this.mapService.buildEmbedUrl(lat, lng);
 */
@Injectable({
  providedIn: 'root'
})
export class MapService {

  /**
   * Construye la URL de embed de OpenStreetMap para un punto dado.
   * @param lat  Latitud del marcador
   * @param lng  Longitud del marcador
   * @param zoom Margen del bbox en grados (por defecto 0.01 ≈ ~1 km)
   * @returns URL lista para usar en un iframe, o '' si las coordenadas son inválidas
   *
   * TODO ALOJ-18 (Sprint 4 - Búsqueda por ubicación):
   * Aprovechar el parámetro zoom para ajustar el nivel de detalle según contexto:
   *   - Vista de ciudad:            zoom = 0.1  (~10 km)
   *   - Vista de barrio/alojamiento: zoom = 0.01 (~1 km, actual por defecto)
   *   - Vista de país/región:        zoom = 1.0  (~100 km)
   */
  buildEmbedUrl(lat: number | null | undefined, lng: number | null | undefined, zoom = 0.01): string {
    if (!lat || !lng) return '';
    return (
      `https://www.openstreetmap.org/export/embed.html` +
      `?bbox=${lng - zoom},${lat - zoom},${lng + zoom},${lat + zoom}` +
      `&layer=mapnik&marker=${lat},${lng}`
    );
  }
}