import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService }        from '../../../../../services/AuthService';
import { Alojamiento }        from '../../../../models/alojamiento.model';

/**
 * PanelGestionPageComponent — ALOJ-9
 *
 * Dashboard exclusivo del anfitrión:
 * - Lista sus alojamientos (GET /api/alojamientos/anfitrion/:hostId)
 * - Acceso directo a Crear, Editar y Eliminar (soft delete)
 * - Confirmación modal antes de eliminar
 * - Estados: cargando, vacío, error
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

  // ── Carga ────────────────────────────────────────────────────────

  cargarAlojamientos(): void {
    const usuario = this.authService.getUsuario();
    const hostId  = usuario?.id;

    if (!hostId) {
      this.error = 'No se pudo identificar al anfitrión. Por favor inicia sesión nuevamente.';
      return;
    }

    this.cargando = true;
    this.error    = '';

    this.alojamientoService.getByAnfitrion(hostId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.alojamientos = data;
          this.cargando     = false;
        },
        error: (err: Error) => {
          // 204 No Content llega como error en algunos entornos: tratar como lista vacía
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
    this.router.navigate(['/alojamientos', id, 'editar']);
  }

  irADetalle(id: number): void {
    this.router.navigate(['/alojamientos', id]);
  }

  // ── Eliminación con modal de confirmación ─────────────────────────

  /**
   * ALOJ-9: Abre el modal de confirmación para el alojamiento seleccionado.
   * Detiene la propagación para no activar irADetalle() de la fila.
   */
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

  get precioPromedio(): string {
    if (this.alojamientos.length === 0) return '—';
    const suma = this.alojamientos.reduce((acc, a) => acc + (a.pricePerNight ?? 0), 0);
    return (suma / this.alojamientos.length).toLocaleString('es-CO', { maximumFractionDigits: 0 });
  }

  precioFormateado(precio: number): string {
    return precio?.toLocaleString('es-CO') ?? '0';
  }

  readonly placeholderImg = 'https://placehold.co/400x260/e2e8f0/94a3b8?text=Sin+imagen';

  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }
}
