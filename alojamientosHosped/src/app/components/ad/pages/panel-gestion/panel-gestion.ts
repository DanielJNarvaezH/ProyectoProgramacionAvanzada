import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError } from 'rxjs';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { ReservaService }     from '../../../../../services/ReservaService';
import { AuthService }        from '../../../../../services/AuthService';
import { Alojamiento }        from '../../../../models/alojamiento.model';
import { Reserva, ESTADO_RESERVA_LABEL, ESTADO_RESERVA_COLOR } from '../../../../models/reserva.model';

/**
 * PanelGestionPageComponent â€” ALOJ-9 + Fix-4 + RESERV-9
 *
 * RESERV-9: Dashboard de anfitriÃ³n con vista de reservas de sus alojamientos.
 * Tabs: Mis alojamientos | Reservas recibidas
 */
@Component({
  selector: 'app-panel-gestion',
  standalone: false,
  templateUrl: './panel-gestion.html',
  styleUrls: ['./panel-gestion.scss']
})
export class PanelGestionPageComponent implements OnInit, OnDestroy {

  // â”€â”€ Tab activo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  tabActivo: 'alojamientos' | 'reservas' = 'alojamientos';

  // â”€â”€ Alojamientos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  alojamientos: Alojamiento[] = [];
  cargando       = false;
  error          = '';
  successMessage = '';

  // Modal de confirmaciÃ³n de eliminaciÃ³n
  mostrarModal          = false;
  alojamientoAEliminar: Alojamiento | null = null;
  eliminando            = false;
  errorEliminacion      = '';
  reactivando: number | null = null;

  // â”€â”€ RESERV-9: Reservas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
  reservas:          Reserva[] = [];
  cargandoReservas   = false;
  errorReservas      = '';
  filtroEstado = 'TODAS';
  readonly ESTADOS   = ['TODAS', 'PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA'];
  readonly ESTADO_LABEL = ESTADO_RESERVA_LABEL;
  readonly ESTADO_COLOR = ESTADO_RESERVA_COLOR;

  // Mapa lodgingId â†’ nombre del alojamiento
  nombreAlojamiento = new Map<number, string>();

  private destroy$ = new Subject<void>();

  constructor(
    private alojamientoService: AlojamientoService,
    private reservaService:     ReservaService,
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

  // â”€â”€ Tabs â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  cambiarTab(tab: 'alojamientos' | 'reservas'): void {
    this.tabActivo = tab;
    if (tab === 'reservas' && this.reservas.length === 0) {
      this.cargarReservas();
    }
  }

  // â”€â”€ Carga de alojamientos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  cargarAlojamientos(): void {
    const usuario = this.authService.getUsuario();
    const hostId  = usuario?.id;
    if (!hostId) {
      this.error = 'No se pudo identificar al anfitriÃ³n.';
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
          // Construir mapa nombre para usarlo en la vista de reservas
          data.forEach(a => {
            if (a.id) this.nombreAlojamiento.set(a.id, a.name);
          });
        },
        error: (err: Error) => {
          this.alojamientos = [];
          this.error        = err.message?.includes('Http failure')
            ? '' : (err.message || 'Error al cargar los alojamientos');
          this.cargando     = false;
        }
      });
  }

  // â”€â”€ RESERV-9: Carga de reservas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  cargarReservas(): void {
    if (this.alojamientos.length === 0) return;

    this.cargandoReservas = true;
    this.errorReservas    = '';

    const ids = this.alojamientos
      .map(a => a.id!)
      .filter(id => !!id);

    // Una peticiÃ³n por alojamiento, en paralelo
    const peticiones = ids.map(id =>
      this.reservaService.getByAlojamiento(id).pipe(
        catchError(() => of([] as Reserva[]))
      )
    );

    forkJoin(peticiones)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resultados) => {
          // Aplanar y ordenar por fecha de inicio descendente
          this.reservas = resultados
            .flat()
            .sort((a, b) => b.startDate.localeCompare(a.startDate));
          this.cargandoReservas = false;
        },
        error: () => {
          this.errorReservas    = 'Error al cargar las reservas.';
          this.cargandoReservas = false;
        }
      });
  }

  // â”€â”€ RESERV-9: Filtrado de reservas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  get reservasFiltradas(): Reserva[] {
    if (this.filtroEstado === 'TODAS') return this.reservas;
    return this.reservas.filter(r => r.status === this.filtroEstado);
  }

  get totalReservas(): number { return this.reservas.length; }

  get totalPendientes(): number {
    return this.reservas.filter(r => r.status === 'PENDIENTE').length;
  }

  get totalConfirmadas(): number {
    return this.reservas.filter(r => r.status === 'CONFIRMADA').length;
  }

  get ingresosTotales(): string {
    const total = this.reservas
      .filter(r => r.status === 'CONFIRMADA' || r.status === 'COMPLETADA')
      .reduce((acc, r) => acc + (r.totalPrice ?? 0), 0);
    return total.toLocaleString('es-CO', { maximumFractionDigits: 0 });
  }

  getNombreAlojamiento(lodgingId: number): string {
    return this.nombreAlojamiento.get(lodgingId) ?? `Alojamiento #${lodgingId}`;
  }

  formatearFecha(fecha: string): string {
    return new Date(fecha + 'T00:00:00').toLocaleDateString('es-CO', {
      day: 'numeric', month: 'short', year: 'numeric'
    });
  }

  calcularNoches(startDate: string, endDate: string): number {
    const inicio = new Date(startDate + 'T00:00:00');
    const fin    = new Date(endDate   + 'T00:00:00');
    return Math.round((fin.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
  }

  // â”€â”€ NavegaciÃ³n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  irACrear(): void { this.router.navigate(['/alojamientos/crear']); }

  irAEditar(id: number): void {
    this.router.navigate(['/alojamientos', id, 'editar'],
      { queryParams: { origen: '/mis-alojamientos' } });
  }

  irADetalle(id: number): void {
    this.router.navigate(['/alojamientos', id],
      { queryParams: { origen: '/mis-alojamientos' } });
  }

  // â”€â”€ Reactivar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  reactivarAlojamiento(event: Event, aloj: Alojamiento): void {
    event.stopPropagation();
    if (!aloj.id) return;
    this.reactivando = aloj.id;
    this.alojamientoService.update(aloj.id, { ...aloj, active: true })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.alojamientos   = this.alojamientos.map(a =>
            a.id === aloj.id ? { ...a, active: true } : a);
          this.reactivando    = null;
          this.successMessage = `"${aloj.name}" fue reactivado correctamente.`;
          setTimeout(() => this.successMessage = '', 4000);
        },
        error: (err: Error) => {
          this.reactivando = null;
          this.error       = err.message || 'No se pudo reactivar.';
          setTimeout(() => this.error = '', 4000);
        }
      });
  }

  // â”€â”€ EliminaciÃ³n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
          this.alojamientos   = this.alojamientos.filter(
            a => a.id !== this.alojamientoAEliminar!.id);
          this.eliminando     = false;
          this.mostrarModal   = false;
          this.successMessage = `"${this.alojamientoAEliminar!.name}" fue eliminado.`;
          this.alojamientoAEliminar = null;
          setTimeout(() => this.successMessage = '', 4000);
        },
        error: (err: Error) => {
          this.eliminando       = false;
          this.errorEliminacion = err.message || 'No se pudo eliminar.';
        }
      });
  }

  // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  get nombreAnfitrionLabel(): string {
    return this.authService.getUsuario()?.name?.split(' ')[0] || 'AnfitriÃ³n';
  }

  get totalAlojamientos(): number { return this.alojamientos.length; }
  get totalActivos(): number { return this.alojamientos.filter(a => a.active).length; }
  get totalInactivos(): number { return this.alojamientos.filter(a => !a.active).length; }

  get precioPromedio(): string {
    const activos = this.alojamientos.filter(a => a.active);
    if (activos.length === 0) return 'â€”';
    const suma = activos.reduce((acc, a) => acc + (a.pricePerNight ?? 0), 0);
    return (suma / activos.length).toLocaleString('es-CO', { maximumFractionDigits: 0 });
  }

  readonly placeholderImg = 'https://placehold.co/400x260/e2e8f0/94a3b8?text=Sin+imagen';
  getLabelEstado(estado: string): string {
    if (estado === 'TODAS') return 'Todas';
    const labels: Record<string, string> = this.ESTADO_LABEL;
    return labels[estado] ?? estado;
  }

  getColorEstado(estado: string): string {
    const colors: Record<string, string> = this.ESTADO_COLOR;
    return colors[estado] ?? 'info';
  }

  setFiltroEstado(estado: string): void {
    this.filtroEstado = estado;
  }
  trackById(_: number, item: Alojamiento): number | undefined { return item.id; }
  trackByReservaId(_: number, item: Reserva): number | undefined { return item.id; }
}
