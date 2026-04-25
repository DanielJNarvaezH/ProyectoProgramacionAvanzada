import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService }        from '../../../../../services/AlojamientoService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { FiltroListaService }         from '../../../../../services/FiltroListaService';
import { ComentarioService }          from '../../../../../services/ComentarioService';
import { Alojamiento, ServicioDisponible, AlojamientoServicio } from '../../../../models';

/**
 * AlojamientosListaPageComponent — ALOJ-4 / ALOJ-6 / ALOJ-18 / ALOJ-19 / ALOJ-23 / COMENT-5
 *
 * COMENT-5: Carga promedios de calificación por alojamiento para mostrarlos
 *   en las tarjetas del listado.
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

  // COMENT-5: Mapa alojamientoId → promedio de calificación
  promedios = new Map<number, number>();
  totalResenas = new Map<number, number>();

  // ── ALOJ-18: Búsqueda por ubicación ──────────────────────────
  buscandoUbicacion = false;
  errorUbicacion    = '';
  readonly RADIOS_DISPONIBLES = [5, 10, 25, 50];

  get modoUbicacion(): boolean  { return this.filtroSvc.modoUbicacion; }
  set modoUbicacion(v: boolean) { this.filtroSvc.modoUbicacion = v; }

  get radioKm(): number  { return this.filtroSvc.radioKm; }
  set radioKm(v: number) { this.filtroSvc.radioKm = v; }

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

  private mapaServicioAlojamientos = new Map<number, Set<number>>();
  private destroy$ = new Subject<void>();

  constructor(
    private alojamientoService:     AlojamientoService,
    private alojamientoServicioSvc: AlojamientoServicioService,
    private filtroSvc:              FiltroListaService,
    private comentarioService:      ComentarioService
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

          // COMENT-5: cargar promedios en paralelo
          this.cargarPromedios(alojamientos.map(a => a.id!).filter(id => !!id));

          if (this.filtroSvc.modoUbicacion && this.filtroSvc.alojamientosCercanos.length > 0) {
            this.alojamientos          = this.filtroSvc.alojamientosCercanos;
            this.alojamientosFiltrados = this.filtroSvc.alojamientosCercanos;
            if (this.filtroSvc.hayFiltroActivo) this.filtrar();
          } else if (this.filtroSvc.hayFiltroActivo) {
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

  // ── COMENT-5: Cargar promedios de calificación ────────────────

  private cargarPromedios(ids: number[]): void {
    if (ids.length === 0) return;

    const peticionesPromedio = ids.map(id =>
      this.comentarioService.getPromedio(id).pipe(catchError(() => of(0)))
    );
    const peticionesComentarios = ids.map(id =>
      this.comentarioService.getByAlojamiento(id).pipe(catchError(() => of([])))
    );

    forkJoin(peticionesPromedio)
      .pipe(takeUntil(this.destroy$))
      .subscribe(promedios => {
        promedios.forEach((p, idx) => this.promedios.set(ids[idx], p ?? 0));
      });

    forkJoin(peticionesComentarios)
      .pipe(takeUntil(this.destroy$))
      .subscribe(resultados => {
        resultados.forEach((lista, idx) => this.totalResenas.set(ids[idx], lista.length));
      });
  }

  /** Devuelve el promedio de calificación de un alojamiento */
  getPromedio(id: number): number {
    return this.promedios.get(id) ?? 0;
  }
  getTotalResenas(id: number): number {
    return this.totalResenas.get(id) ?? 0;
  }

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
    const estabaModoUbicacion = this.modoUbicacion;
    this.filtroSvc.limpiar();
    this.errorUbicacion = '';
    if (estabaModoUbicacion) {
      this.cargarDatos();
    } else {
      this.alojamientosFiltrados = this.alojamientos;
    }
  }

  limpiarTerminoBusqueda(): void {
    this.terminoBusqueda = '';
    this.filtrar();
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
              this.buscandoUbicacion             = false;
              this.modoUbicacion                 = true;
              this.filtroSvc.alojamientosCercanos = resultados;
              this.alojamientos                  = resultados;
              this.alojamientosFiltrados         = resultados;
              this.paginaActual                  = 1;
              if (this.filtroSvc.hayFiltroActivo) this.filtrar();
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
    this.modoUbicacion                  = false;
    this.errorUbicacion                 = '';
    this.filtroSvc.alojamientosCercanos = [];
    this.alojamientos                   = [];
    this.alojamientosFiltrados          = [];
    this.paginaActual                   = 1;
    this.cargarDatos();
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
