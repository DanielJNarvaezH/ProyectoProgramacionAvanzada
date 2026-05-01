import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ReservaService }     from '../../../../../services/ReservaService';
import { AuthService }        from '../../../../../services/AuthService';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { Reserva, EstadoReserva, ESTADO_RESERVA_LABEL, ESTADO_RESERVA_COLOR } from '../../../../models';
import { Alojamiento } from '../../../../models';
import { ToastService } from '../../../../../services/ToastService';

@Component({
  selector: 'app-mis-reservas',
  standalone: false,
  templateUrl: './mis-reservas.html',
  styleUrls: ['./mis-reservas.scss']
})
export class MisReservasPageComponent implements OnInit, OnDestroy {

  reservas:          Reserva[]    = [];
  reservasFiltradas: Reserva[]    = [];
  nombreAlojamiento  = new Map<number, string>();
  cargando           = false;
  error              = '';

  filtroEstado: EstadoReserva | 'TODAS' = 'TODAS';

  readonly FILTROS: { label: string; valor: EstadoReserva | 'TODAS' }[] = [
    { label: 'Todas',      valor: 'TODAS'      },
    { label: 'Confirmada', valor: 'CONFIRMADA' },
    { label: 'Pendiente',  valor: 'PENDIENTE'  },
    { label: 'Completada', valor: 'COMPLETADA' },
    { label: 'Cancelada',  valor: 'CANCELADA'  },
  ];

  mostrarModalCancelar: boolean        = false;
  reservaSeleccionada:  Reserva | null = null;
  cancelando:           boolean        = false;
  errorCancelacion:     string         = '';

  private destroy$ = new Subject<void>();

  constructor(
    private reservaService:     ReservaService,
    private alojamientoService: AlojamientoService,
    private authService:        AuthService,
    public  router:             Router,
    private toastService:       ToastService
  ) {}

  ngOnInit(): void { this.cargarReservas(); }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarReservas(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id) { this.error = 'No se pudo identificar al usuario.'; return; }

    this.cargando = true;
    this.error    = '';

    this.reservaService.getByUser(usuario.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (reservas) => {
          this.reservas = reservas.sort((a, b) => {
            const fa = a.reservationDate ?? a.startDate;
            const fb = b.reservationDate ?? b.startDate;
            return fb.localeCompare(fa);
          });
          this.aplicarFiltro();
          this.cargando = false;
          this.cargarNombresAlojamientos(reservas);
        },
        error: (err) => {
          if (err.message?.includes('no encontr') || err.message?.includes('404')) {
            this.reservas = []; this.reservasFiltradas = [];
          } else {
            this.error = err.message || 'Error al cargar las reservas.';
          }
          this.cargando = false;
        }
      });
  }

  private cargarNombresAlojamientos(reservas: Reserva[]): void {
    const idsUnicos = [...new Set(reservas.map(r => r.lodgingId))];
    idsUnicos.forEach(id => {
      this.alojamientoService.getById(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (aloj: Alojamiento) => this.nombreAlojamiento.set(id, aloj.name),
          error: ()                  => this.nombreAlojamiento.set(id, `Alojamiento #${id}`)
        });
    });
  }

  cambiarFiltro(estado: EstadoReserva | 'TODAS'): void {
    this.filtroEstado = estado;
    this.aplicarFiltro();
  }

  private aplicarFiltro(): void {
    this.reservasFiltradas = this.filtroEstado === 'TODAS'
      ? this.reservas
      : this.reservas.filter(r => r.status === this.filtroEstado);
  }

  puedeCancelar(reserva: Reserva): boolean {
    return reserva.status === 'CONFIRMADA' || reserva.status === 'PENDIENTE';
  }

  abrirModalCancelar(reserva: Reserva): void {
    this.reservaSeleccionada  = reserva;
    this.errorCancelacion     = '';
    this.mostrarModalCancelar = true;
  }

  cerrarModalCancelar(): void {
    if (this.cancelando) return;
    this.mostrarModalCancelar = false;
    this.reservaSeleccionada  = null;
    this.errorCancelacion     = '';
  }

  confirmarCancelacion(motivo: string): void {
    if (!this.reservaSeleccionada?.id || !motivo) return;

    this.cancelando       = true;
    this.errorCancelacion = '';

    this.reservaService.cancel(this.reservaSeleccionada.id, motivo)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const id = this.reservaSeleccionada!.id!;
          this.reservas = this.reservas.map(r =>
            r.id === id
              ? { ...r, status: 'CANCELADA' as EstadoReserva, cancelReason: motivo }
              : r
          );
          this.aplicarFiltro();
          this.cancelando           = false;
          this.mostrarModalCancelar = false;
          this.reservaSeleccionada  = null;
          this.errorCancelacion     = '';
          // Toast SOLO cuando el backend confirma OK
          this.toastService.success('Reserva cancelada correctamente.');
        },
        error: (err: Error) => {
          this.cancelando       = false;
          this.errorCancelacion = err.message || 'No se pudo cancelar la reserva.';
          this.toastService.error(this.errorCancelacion);
        }
      });
  }

  contarPorEstado(estado: EstadoReserva | 'TODAS'): number {
    if (estado === 'TODAS') return this.reservas.length;
    return this.reservas.filter(r => r.status === estado).length;
  }

  getNombreAlojamiento(lodgingId: number): string {
    return this.nombreAlojamiento.get(lodgingId) ?? '...';
  }

  getEstadoLabel(status: EstadoReserva): string { return ESTADO_RESERVA_LABEL[status] ?? status; }
  getEstadoClass(status: EstadoReserva): string { return ESTADO_RESERVA_COLOR[status] ?? ''; }

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

  irAlDetalle(lodgingId: number): void { this.router.navigate(['/alojamientos', lodgingId]); }

  get totalReservas(): number { return this.reservasFiltradas.length; }
  get hayReservas(): boolean  { return this.reservas.length > 0; }

  trackById(_: number, item: Reserva): number | undefined { return item.id; }
}