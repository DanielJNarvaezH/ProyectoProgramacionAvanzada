import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { FiltroListaService } from '../../../../../services/FiltroListaService';
import { Alojamiento, ServicioDisponible, AlojamientoServicio } from '../../../../models';

/**
 * AlojamientosListaPageComponent — ALOJ-4 / ALOJ-6 / ALOJ-18 / ALOJ-19 / ALOJ-23
 *
 * ALOJ-23: Ordenamiento de resultados por precio y capacidad.
 *
 * ALOJ-18: Búsqueda por ubicación — botón "Cerca de mí" que usa la
 *   Geolocation API del navegador y llama al endpoint
 *   GET /api/alojamientos/cercanos?lat=&lng=&radio=
 *
 * ALOJ-19: Filtros avanzados frontend sobre el array ya cargado:
 *   - Rango de precio (precioMin / precioMax)
 *   - Capacidad mínima de huéspedes
 *   - Servicios disponibles (checkboxes múltiples)
 * El estado de los filtros se preserva entre navegaciones gracias
 * a FiltroListaService (lista → detalle → lista mantiene filtros).
 * Paginación frontend: ITEMS_POR_PAGINA alojamientos por página.
 */
@Component({
  selector: 'app-alojamientos-lista',
  standalone: false,
  templateUrl: './alojamientos-lista.html',
  styleUrls: ['./alojamientos-lista.scss']
})
export class AlojamientosListaPageComponent implements OnInit, OnDestroy {

  readonly ITEMS_POR_PAGINA = 8;

  alojamientos: Alojamiento[]            = [];
  alojamientosFiltrados: Alojamiento[]   = [];
  serviciosDisponibles: ServicioDisponible[] = [];

  cargando = false;
  error    = '';

  // ── ALOJ-18: Búsqueda por ubicación ──────────────────────────
  buscandoUbicacion = false;
  errorUbicacion    = '';
  modoUbicacion     = false;
  radioKm           = 10;
  readonly RADIOS_DISPONIBLES = [5, 10, 25, 50];

  // ── ALOJ-19: Estado de filtros — delegado a FiltroListaService ──

  get terminoBusqueda():         string            { return this.filtroSvc.terminoBusqueda; }
  set terminoBusqueda(v:         string)            { this.filtroSvc.terminoBusqueda = v; }

  get precioMin():               number | null      { return this.filtroSvc.precioMin; }
  set precioMin(v:               number | null)     { this.filtroSvc.precioMin = v; }

  get precioMax():               number | null      { return this.filtroSvc.precioMax; }
  set precioMax(v:               number | null)     { this.filtroSvc.precioMax = v; }

  get capacidadMin():            number | null      { return this.filtroSvc.capacidadMin; }
  set capacidadMin(v:            number | null)     { this.filtroSvc.capacidadMin = v; }

  get serviciosSeleccionados():  number[]           { return this.filtroSvc.serviciosSeleccionados; }
  set serviciosSeleccionados(v:  number[])          { this.filtroSvc.serviciosSeleccionados = v; }

  get mostrarFiltros():          boolean            { return this.filtroSvc.mostrarFiltros; }
  set mostrarFiltros(v:          boolean)           { this.filtroSvc.mostrarFiltros = v; }

  get paginaActual():            number             { return this.filtroSvc.paginaActual; }
  set paginaActual(v:            number)            { this.filtroSvc.paginaActual = v; }

  // ── ALOJ-23: Ordenamiento ─────────────────────────────────────
  get ordenamiento(): string { return this.filtroSvc.ordenamiento; }
  set ordenamiento(v: any)   { this.filtroSvc.ordenamiento = v; }

  // Mapa servicioId → Set de alojamientoIds que lo tienen (ALOJ-19)
  private mapaServicioAlojamientos = new Map<number, Set<number>>();

  private destroy$ = new Subject<void>();

  constructor(
    private alojamientoService:     AlojamientoService,
    private alojamientoServicioSvc: AlojamientoServicioService,
    private filtroSvc:              FiltroListaService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Carga inicial ─────────────────────────────────────────────

  cargarDatos(): void {
    this.cargando = true;
    this.error    = '';

    forkJoin({
      alojamientos: this.alojamientoService.getAll(),
      servicios:    this.alojamientoServicioSvc.getServiciosDisponibles()
        .pipe(catchError(() => of([] as ServicioDisponible[])))
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ alojamientos, servicios }) => {
          this.alojamientos         = alojamientos;
          this.serviciosDisponibles = servicios.filter(s => s.active);
          this.cargando             = false;
          this.construirMapaServicios(this.serviciosDisponibles.map(s => s.id));
          if (this.filtroSvc.hayFiltroActivo) {
            this.filtrar();
          } else {
            this.alojamientosFiltrados = alojamientos;
          }
        },
        error: (err) => {
          this.error    = err.message || 'Error al cargar los alojamientos';
          this.cargando = false;
        }
      });
  }

  cargarAlojamientos(): void { this.cargarDatos(); }

  // ── ALOJ-19: Construir mapa servicioId → Set<alojamientoId> ──

  private construirMapaServicios(servicioIds: number[]): void {
    this.mapaServicioAlojamientos.clear();
    if (servicioIds.length === 0) return;

    const peticiones = servicioIds.map(id =>
      this.alojamientoServicioSvc.getAlojamientosByServicio(id)
        .pipe(catchError(() => of([] as AlojamientoServicio[])))
    );

    forkJoin(peticiones)
      .pipe(takeUntil(this.destroy$))
      .subscribe(resultados => {
        resultados.forEach((lista, idx) => {
          const servicioId = servicioIds[idx];
          const alojIds    = new Set(lista.map(r => r.lodgingId));
          this.mapaServicioAlojamientos.set(servicioId, alojIds);
        });
        if (this.serviciosSeleccionados.length > 0) this.filtrar();
      });
  }

  // ── ALOJ-6 + ALOJ-19 + ALOJ-23: Filtrado y ordenamiento ──────

  filtrar(): void {
    this.paginaActual = 1;
    const termino     = this.terminoBusqueda.trim().toLowerCase();

    this.alojamientosFiltrados = this.alojamientos.filter(a => {
      if (termino) {
        const coincideTexto =
          a.name.toLowerCase().includes(termino) ||
          a.city.toLowerCase().includes(termino);
        if (!coincideTexto) return false;
      }
      if (this.precioMin !== null && a.pricePerNight < this.precioMin) return false;
      if (this.precioMax !== null && a.pricePerNight > this.precioMax) return false;
      if (this.capacidadMin !== null && a.maxCapacity < this.capacidadMin) return false;
      if (this.serviciosSeleccionados.length > 0) {
        const tieneTodos = this.serviciosSeleccionados.every(sId => {
          const alojIds = this.mapaServicioAlojamientos.get(sId);
          return alojIds ? alojIds.has(a.id!) : false;
        });
        if (!tieneTodos) return false;
      }
      return true;
    });

    // ALOJ-23: Ordenamiento de resultados
    this.alojamientosFiltrados = [...this.alojamientosFiltrados].sort((a, b) => {
      switch (this.ordenamiento) {
        case 'precio-asc':     return a.pricePerNight - b.pricePerNight;
        case 'precio-desc':    return b.pricePerNight - a.pricePerNight;
        case 'capacidad-asc':  return a.maxCapacity - b.maxCapacity;
        case 'capacidad-desc': return b.maxCapacity - a.maxCapacity;
        default: return 0;
      }
    });
  }

  limpiarFiltro(): void {
    this.filtroSvc.limpiar();
    this.modoUbicacion  = false;
    this.errorUbicacion = '';
    this.alojamientosFiltrados = this.alojamientos;
  }

  // ── ALOJ-18: Búsqueda por ubicación ──────────────────────────

  buscarCercaDeMi(): void {
    if (!navigator.geolocation) {
      this.errorUbicacion = 'Tu navegador no soporta geolocalización.';
      return;
    }

    this.buscandoUbicacion = true;
    this.errorUbicacion    = '';

    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        const { latitude, longitude } = posicion.coords;

        this.alojamientoService.getCercanos(latitude, longitude, this.radioKm)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (resultados) => {
              this.buscandoUbicacion     = false;
              this.modoUbicacion         = true;
              this.alojamientosFiltrados = resultados;
              this.paginaActual          = 1;
              this.filtroSvc.limpiar();
            },
            error: (err: Error) => {
              this.buscandoUbicacion = false;
              this.errorUbicacion    = err.message || 'Error al buscar alojamientos cercanos.';
            }
          });
      },
      (geoError) => {
        this.buscandoUbicacion = false;
        switch (geoError.code) {
          case GeolocationPositionError.PERMISSION_DENIED:
            this.errorUbicacion = 'Permiso de ubicación denegado. Actívalo en tu navegador.';
            break;
          case GeolocationPositionError.POSITION_UNAVAILABLE:
            this.errorUbicacion = 'No se pudo obtener tu ubicación actual.';
            break;
          case GeolocationPositionError.TIMEOUT:
            this.errorUbicacion = 'La solicitud de ubicación tardó demasiado.';
            break;
          default:
            this.errorUbicacion = 'Error desconocido al obtener la ubicación.';
        }
      },
      { timeout: 10000, maximumAge: 60000 }
    );
  }

  cambiarRadio(nuevoRadio: number): void {
    this.radioKm = nuevoRadio;
    if (this.modoUbicacion) this.buscarCercaDeMi();
  }

  salirModoUbicacion(): void {
    this.modoUbicacion         = false;
    this.errorUbicacion        = '';
    this.alojamientosFiltrados = this.alojamientos;
    this.paginaActual          = 1;
  }

  toggleFiltros(): void {
    this.mostrarFiltros = !this.mostrarFiltros;
  }

  // ── ALOJ-19: Manejo de servicios seleccionados ───────────────

  toggleServicio(servicioId: number): void {
    const idx = this.serviciosSeleccionados.indexOf(servicioId);
    if (idx === -1) {
      this.serviciosSeleccionados = [...this.serviciosSeleccionados, servicioId];
    } else {
      this.serviciosSeleccionados = this.serviciosSeleccionados.filter(id => id !== servicioId);
    }
    this.filtrar();
  }

  estaSeleccionado(servicioId: number): boolean {
    return this.serviciosSeleccionados.includes(servicioId);
  }

  // ── Paginación ────────────────────────────────────────────────

  get totalPaginas(): number {
    return Math.ceil(this.alojamientosFiltrados.length / this.ITEMS_POR_PAGINA);
  }

  get alojamientosPagina(): Alojamiento[] {
    const inicio = (this.paginaActual - 1) * this.ITEMS_POR_PAGINA;
    return this.alojamientosFiltrados.slice(inicio, inicio + this.ITEMS_POR_PAGINA);
  }

  get numeroPaginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  irAPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas) return;
    this.paginaActual = pagina;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  paginaAnterior(): void  { this.irAPagina(this.paginaActual - 1); }
  paginaSiguiente(): void { this.irAPagina(this.paginaActual + 1); }

  // ── Helpers ───────────────────────────────────────────────────

  get totalMostrados(): number {
    return this.alojamientosFiltrados.length;
  }

  get hayFiltroActivo(): boolean {
    return this.filtroSvc.hayFiltroActivo;
  }

  get cantidadFiltrosActivos(): number {
    let count = 0;
    if (this.filtroSvc.terminoBusqueda.trim()) count++;
    if (this.filtroSvc.precioMin    !== null)  count++;
    if (this.filtroSvc.precioMax    !== null)  count++;
    if (this.filtroSvc.capacidadMin !== null)  count++;
    count += this.filtroSvc.serviciosSeleccionados.length;
    return count;
  }

  get idsAlojamientosFiltrados(): number[] {
    return this.alojamientosFiltrados
      .map(a => a.id!)
      .filter(id => !!id);
  }

  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }

  trackByServicioId(_: number, s: ServicioDisponible): number {
    return s.id;
  }
}
