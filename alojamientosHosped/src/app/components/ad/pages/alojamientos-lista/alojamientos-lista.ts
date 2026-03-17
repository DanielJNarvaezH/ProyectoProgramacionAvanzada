import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { Alojamiento } from '../../../../models/alojamiento.model';

/**
 * AlojamientosListaPageComponent — ALOJ-4 / ALOJ-6
 *
 * Paginación frontend: muestra ITEMS_POR_PAGINA alojamientos por página.
 * No requiere cambios en el backend — pagina el array ya cargado.
 */
@Component({
  selector: 'app-alojamientos-lista',
  standalone: false,
  templateUrl: './alojamientos-lista.html',
  styleUrls: ['./alojamientos-lista.scss']
})
export class AlojamientosListaPageComponent implements OnInit, OnDestroy {

  readonly ITEMS_POR_PAGINA = 8;

  alojamientos: Alojamiento[]          = [];
  alojamientosFiltrados: Alojamiento[] = [];

  cargando        = false;
  error           = '';
  terminoBusqueda = '';

  // ── Paginación ────────────────────────────────────────────────
  paginaActual = 1;

  private destroy$ = new Subject<void>();

  constructor(private alojamientoService: AlojamientoService) {}

  ngOnInit(): void {
    this.cargarAlojamientos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarAlojamientos(): void {
    this.cargando = true;
    this.error    = '';

    this.alojamientoService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.alojamientos          = data;
          this.alojamientosFiltrados = data;
          this.paginaActual          = 1;
          this.cargando              = false;
        },
        error: (err) => {
          this.error    = err.message || 'Error al cargar los alojamientos';
          this.cargando = false;
        }
      });
  }

  // ── Búsqueda ──────────────────────────────────────────────────

  filtrar(): void {
    const termino = this.terminoBusqueda.trim().toLowerCase();
    this.paginaActual = 1; // volver a página 1 al filtrar
    if (!termino) {
      this.alojamientosFiltrados = this.alojamientos;
      return;
    }
    this.alojamientosFiltrados = this.alojamientos.filter(a =>
      a.name.toLowerCase().includes(termino) ||
      a.city.toLowerCase().includes(termino)
    );
  }

  limpiarFiltro(): void {
    this.terminoBusqueda       = '';
    this.alojamientosFiltrados = this.alojamientos;
    this.paginaActual          = 1;
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

  paginaAnterior(): void {
    this.irAPagina(this.paginaActual - 1);
  }

  paginaSiguiente(): void {
    this.irAPagina(this.paginaActual + 1);
  }

  // ── Helpers ───────────────────────────────────────────────────

  get totalMostrados(): number {
    return this.alojamientosFiltrados.length;
  }

  get hayFiltroActivo(): boolean {
    return this.terminoBusqueda.trim().length > 0;
  }

  get idsAlojamientosFiltrados(): number[] {
    return this.alojamientosFiltrados
      .map(a => a.id!)
      .filter(id => !!id);
  }

  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }
}