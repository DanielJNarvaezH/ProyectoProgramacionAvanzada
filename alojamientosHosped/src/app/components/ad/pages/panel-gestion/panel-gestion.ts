import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService }        from '../../../../../services/AuthService';
import { Alojamiento }        from '../../../../models/alojamiento.model';

/**
 * PanelGestionPageComponent — ALOJ-9 + Fix-4
 *
 * Fix-4: carga TODOS los alojamientos del anfitrión (activos e inactivos)
 * para que pueda ver y reactivar los que desactivó.
 */
@Component({
  selector: 'app-panel-gestion',
  standalone: false,
  templateUrl: './panel-gestion.html',
  styleUrls: ['./panel-gestion.scss']
})
export class PanelGestionPageComponent implements OnInit, OnDestroy {

  alojamientos: Alojamiento[] = [];

  cargando       = false;
  error          = '';
  successMessage = '';

  // ── Modal de confirmación de eliminación ────────────────────────
  mostrarModal          = false;
  alojamientoAEliminar: Alojamiento | null = null;
  eliminando            = false;
  errorEliminacion      = '';

  // ── Fix-4: reactivar ─────────────────────────────────────────────
  reactivando: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private alojamientoService: AlojamientoService,
    private authService:        AuthService,
    private router:             Router
  ) {}

  ngOnInit(): void {
    this.cargarAlojamientos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Carga — ahora trae activos E inactivos ────────────────────────

  cargarAlojamientos(): void {
    const usuario = this.authService.getUsuario();
    const hostId  = usuario?.id;

    if (!hostId) {
      this.error = 'No se pudo identificar al anfitrión. Por favor inicia sesión nuevamente.';
      return;
    }

    this.cargando = true;
    this.error    = '';

    this.alojamientoService.getByAnfitrionTodos(hostId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.alojamientos = data;
          this.cargando     = false;
        },
        error: (err: Error) => {
          this.alojamientos = [];
          this.error        = err.message?.includes('Http failure')
            ? ''
            : (err.message || 'Error al cargar los alojamientos');
          this.cargando     = false;
        }
      });
  }

  // ── Navegación ───────────────────────────────────────────────────

  irACrear(): void {
    this.router.navigate(['/alojamientos/crear']);
  }

  irAEditar(id: number): void {
    // Fix: pasar origen para que cancelar en editar vuelva al panel
    this.router.navigate(['/alojamientos', id, 'editar'], { queryParams: { origen: '/mis-alojamientos' } });
  }

  irADetalle(id: number): void {
    // Fix: pasar origen para que el botón Volver del detalle regrese al panel
    this.router.navigate(['/alojamientos', id], { queryParams: { origen: '/mis-alojamientos' } });
  }

  // ── Fix-4: Reactivar alojamiento ─────────────────────────────────

  reactivarAlojamiento(event: Event, aloj: Alojamiento): void {
    event.stopPropagation();
    if (!aloj.id) return;

    this.reactivando = aloj.id;

    const payload: Alojamiento = { ...aloj, active: true };

    this.alojamientoService.update(aloj.id, payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (actualizado) => {
          // Actualizar en la lista local sin recargar
          this.alojamientos = this.alojamientos.map(a =>
            a.id === aloj.id ? { ...a, active: true } : a
          );
          this.reactivando    = null;
          this.successMessage = `"${aloj.name}" fue reactivado correctamente.`;
          setTimeout(() => this.successMessage = '', 4000);
        },
        error: (err: Error) => {
          this.reactivando    = null;
          this.successMessage = '';
          this.error = err.message || 'No se pudo reactivar el alojamiento.';
          setTimeout(() => this.error = '', 4000);
        }
      });
  }

  // ── Eliminación con modal de confirmación ─────────────────────────

  abrirModalEliminar(event: Event, alojamiento: Alojamiento): void {
    event.stopPropagation();
    this.alojamientoAEliminar = alojamiento;
    this.errorEliminacion     = '';
    this.mostrarModal         = true;
  }

  cerrarModal(): void {
    if (this.eliminando) return;
    this.mostrarModal         = false;
    this.alojamientoAEliminar = null;
    this.errorEliminacion     = '';
  }

  confirmarEliminar(): void {
    if (!this.alojamientoAEliminar?.id) return;

    this.eliminando       = true;
    this.errorEliminacion = '';

    this.alojamientoService.delete(this.alojamientoAEliminar.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.alojamientos   = this.alojamientos.filter(a => a.id !== this.alojamientoAEliminar!.id);
          this.eliminando     = false;
          this.mostrarModal   = false;
          this.successMessage = `"${this.alojamientoAEliminar!.name}" fue eliminado correctamente.`;
          this.alojamientoAEliminar = null;
          setTimeout(() => this.successMessage = '', 4000);
        },
        error: (err: Error) => {
          this.eliminando       = false;
          this.errorEliminacion = err.message || 'No se pudo eliminar el alojamiento.';
        }
      });
  }

  // ── Helpers ──────────────────────────────────────────────────────

  get nombreAnfitrion(): string {
    return this.authService.getUsuario()?.name?.split(' ')[0] || 'Anfitrión';
  }

  get totalAlojamientos(): number {
    return this.alojamientos.length;
  }

  // Fix-4: métricas separadas activos/inactivos
  get totalActivos(): number {
    return this.alojamientos.filter(a => a.active).length;
  }

  get totalInactivos(): number {
    return this.alojamientos.filter(a => !a.active).length;
  }

  get precioPromedio(): string {
    const activos = this.alojamientos.filter(a => a.active);
    if (activos.length === 0) return '—';
    const suma = activos.reduce((acc, a) => acc + (a.pricePerNight ?? 0), 0);
    return (suma / activos.length).toLocaleString('es-CO', { maximumFractionDigits: 0 });
  }

  readonly placeholderImg = 'https://placehold.co/400x260/e2e8f0/94a3b8?text=Sin+imagen';

  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }
}