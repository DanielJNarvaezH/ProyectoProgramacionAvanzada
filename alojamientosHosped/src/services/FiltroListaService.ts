import { Injectable } from '@angular/core';

/**
 * FiltroListaService — ALOJ-19 / ALOJ-23
 *
 * Preserva el estado de los filtros de la lista de alojamientos
 * entre navegaciones (lista → detalle → lista).
 *
 * Usa providedIn: 'root' como singleton en memoria — el estado
 * se mantiene mientras la sesión esté activa pero se resetea al
 * refrescar la página (comportamiento esperado).
 *
 * Si en el futuro se necesita persistencia en URL (para compartir
 * búsquedas), migrar a queryParams en ALOJ-18.
 */
@Injectable({
  providedIn: 'root'
})
export class FiltroListaService {

  terminoBusqueda  = '';
  precioMin: number | null = null;
  precioMax: number | null = null;
  capacidadMin: number | null = null;
  serviciosSeleccionados: number[] = [];
  paginaActual     = 1;
  mostrarFiltros   = false;

  // ALOJ-23: Ordenamiento de resultados
  ordenamiento: 'ninguno' | 'precio-asc' | 'precio-desc' | 'capacidad-asc' | 'capacidad-desc' = 'ninguno';

  // ALOJ-18: Estado de búsqueda por ubicación — persiste entre navegaciones
  modoUbicacion:       boolean      = false;
  radioKm:             number       = 10;
  alojamientosCercanos: any[]       = [];   // resultados crudos del endpoint cercanos

  get hayFiltroActivo(): boolean {
    return (
      this.terminoBusqueda.trim().length > 0 ||
      this.precioMin              !== null   ||
      this.precioMax              !== null   ||
      this.capacidadMin           !== null   ||
      this.serviciosSeleccionados.length > 0 ||
      this.ordenamiento           !== 'ninguno' ||
      this.modoUbicacion
    );
  }

  limpiar(): void {
    this.terminoBusqueda        = '';
    this.precioMin              = null;
    this.precioMax              = null;
    this.capacidadMin           = null;
    this.serviciosSeleccionados = [];
    this.paginaActual           = 1;
    this.mostrarFiltros         = false;
    this.ordenamiento           = 'ninguno';
    this.modoUbicacion          = false;
    this.radioKm                = 10;
    this.alojamientosCercanos   = [];
  }
}