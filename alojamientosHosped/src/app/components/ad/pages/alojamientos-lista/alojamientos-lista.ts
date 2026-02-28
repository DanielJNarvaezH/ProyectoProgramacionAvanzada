import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { Alojamiento } from '../../../../models/alojamiento.model';

/**
 * AlojamientosListaPageComponent — ALOJ-4
 *
 * Página de listado de alojamientos en grid responsive.
 * - Llama a AlojamientoService.getAll() al iniciar
 * - Maneja estados: loading, error, vacío, con datos
 * - Permite filtrar por ciudad en tiempo real
 */
@Component({
  selector: 'app-alojamientos-lista',
  standalone: false,
  templateUrl: './alojamientos-lista.html',
  styleUrls: ['./alojamientos-lista.scss']
})
export class AlojamientosListaPageComponent implements OnInit, OnDestroy {

  alojamientos: Alojamiento[]         = [];
  alojamientosFiltrados: Alojamiento[] = [];

  cargando    = false;
  error       = '';
  filtroCiudad = '';

  private destroy$ = new Subject<void>();

  constructor(private alojamientoService: AlojamientoService) {}

  ngOnInit(): void {
    this.cargarAlojamientos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─────────────────────────────────────────────────────────────
  // Carga de datos
  // ─────────────────────────────────────────────────────────────

  cargarAlojamientos(): void {
    this.cargando = true;
    this.error    = '';

    this.alojamientoService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.alojamientos         = data;
          this.alojamientosFiltrados = data;
          this.cargando             = false;
        },
        error: (err) => {
          this.error    = err.message || 'Error al cargar los alojamientos';
          this.cargando = false;
        }
      });
  }

  // ─────────────────────────────────────────────────────────────
  // Filtrado por ciudad
  // ─────────────────────────────────────────────────────────────

  filtrarPorCiudad(): void {
    const termino = this.filtroCiudad.trim().toLowerCase();
    if (!termino) {
      this.alojamientosFiltrados = this.alojamientos;
      return;
    }
    this.alojamientosFiltrados = this.alojamientos.filter(a =>
      a.ciudad.toLowerCase().includes(termino)
    );
  }

  limpiarFiltro(): void {
    this.filtroCiudad          = '';
    this.alojamientosFiltrados = this.alojamientos;
  }

  // ─────────────────────────────────────────────────────────────
  // Helpers de template
  // ─────────────────────────────────────────────────────────────

  get totalMostrados(): number {
    return this.alojamientosFiltrados.length;
  }

  get hayFiltroActivo(): boolean {
    return this.filtroCiudad.trim().length > 0;
  }

  /** Trackby para optimizar el *ngFor */
  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }
}
