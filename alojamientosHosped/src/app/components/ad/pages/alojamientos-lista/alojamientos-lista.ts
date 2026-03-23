import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService }        from '../../../../../services/AlojamientoService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { FiltroListaService }         from '../../../../../services/FiltroListaService';
import { Alojamiento }        from '../../../../models/alojamiento.model';
import { ServicioDisponible } from '../../../../models/servicio.model';
import { AlojamientoServicio } from '../../../../models/alojamiento-servicio.model';

/**
 * AlojamientosListaPageComponent — ALOJ-4 / ALOJ-6 / ALOJ-19
 *
 * ALOJ-19: Filtros avanzados frontend sobre el array ya cargado:
 *   - Rango de precio (precioMin / precioMax)
 *   - Capacidad mínima de huéspedes
 *   - Servicios disponibles (checkboxes múltiples)
 * Se combinan con la búsqueda por texto existente (ALOJ-6).
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

  // ── ALOJ-19: Estado de filtros — delegado a FiltroListaService ──
  // Los getters/setters sincronizan con el servicio para preservar
  // el estado al navegar lista → detalle → lista.

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

  // Mapa servicioId → Set de alojamientoIds que lo tienen (ALOJ-19)
  // Se construye una vez al cargar con GET /servicio/:id/alojamientos
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
          // Construir mapa y luego aplicar filtros (restaura estado si venimos del detalle)
          this.construirMapaServicios(this.serviciosDisponibles.map(s => s.id));
          // Si no hay filtros activos, mostrar todo; si los hay, reaplicarlos
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

  // Alias para el botón "Intentar de nuevo"
  cargarAlojamientos(): void { this.cargarDatos(); }

  // ── ALOJ-19: Construir mapa servicioId → Set<alojamientoId> ──

  private construirMapaServicios(servicioIds: number[]): void {
    this.mapaServicioAlojamientos.clear();
    if (servicioIds.length === 0) return;

    // N llamadas paralelas (N = nº de servicios activos, normalmente 8-15)
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
        // Re-aplicar filtros si ya hay servicios seleccionados
        if (this.serviciosSeleccionados.length > 0) this.filtrar();
      });
  }

  // ── ALOJ-6 + ALOJ-19: Filtrado combinado ─────────────────────

  filtrar(): void {
    this.paginaActual = 1;
    const termino     = this.terminoBusqueda.trim().toLowerCase();

    // Filtrado frontend sobre array ya cargado — consistente con la estrategia
    // de paginación local de ALOJ-4. El backend expone GET /filtro/precio?min=&max=
    // para cuando se migre a paginación real en backend (ver ALOJ-20).
    this.alojamientosFiltrados = this.alojamientos.filter(a => {
      // Búsqueda por texto (ALOJ-6)
      if (termino) {
        const coincideTexto =
          a.name.toLowerCase().includes(termino) ||
          a.city.toLowerCase().includes(termino);
        if (!coincideTexto) return false;
      }
      // Filtro precio mínimo
      if (this.precioMin !== null && a.pricePerNight < this.precioMin) return false;
      // Filtro precio máximo
      if (this.precioMax !== null && a.pricePerNight > this.precioMax) return false;
      // Filtro capacidad mínima
      if (this.capacidadMin !== null && a.maxCapacity < this.capacidadMin) return false;
      // Filtro servicios: el alojamiento debe tener TODOS los servicios seleccionados
      if (this.serviciosSeleccionados.length > 0) {
        const tieneToodos = this.serviciosSeleccionados.every(sId => {
          const alojIds = this.mapaServicioAlojamientos.get(sId);
          return alojIds ? alojIds.has(a.id!) : false;
        });
        if (!tieneToodos) return false;
      }
      return true;
    });
  }

  limpiarFiltro(): void {
    this.filtroSvc.limpiar();
    this.alojamientosFiltrados = this.alojamientos;
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