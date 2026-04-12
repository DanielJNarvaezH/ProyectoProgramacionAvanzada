import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ReservaService }     from '../../../../../services/ReservaService';
import { AuthService }        from '../../../../../services/AuthService';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { Reserva, EstadoReserva, ESTADO_RESERVA_LABEL, ESTADO_RESERVA_COLOR } from '../../../../models';
import { Alojamiento } from '../../../../models';

/**
 * MisReservasPageComponent — RESERV-8 + RESERV-10
 *
 * Historial de reservas del huésped autenticado:
 * - Lista todas las reservas via GET /api/reservas/huesped/:guestId
 * - Enriquece cada reserva con el nombre del alojamiento
 * - Filtro por estado: TODAS | CONFIRMADA | PENDIENTE | CANCELADA | COMPLETADA
 * - Ordenadas por fecha de check-in descendente (más recientes primero)
 *
 * RESERV-10: Función de cancelación de reserva
 * - Botón "Cancelar" visible solo en reservas con estado CONFIRMADA o PENDIENTE
 * - Abre modal con campo de motivo obligatorio (mínimo 10 caracteres)
 * - Llama a DELETE /api/reservas/:id?motivo=... al confirmar
 * - Actualiza la lista localmente al completarse correctamente
 */
@Component({
  selector: 'app-mis-reservas',
  standalone: false,
  templateUrl: './mis-reservas.html',
  styleUrls: ['./mis-reservas.scss']
})
export class MisReservasPageComponent implements OnInit, OnDestroy {

  reservas:          Reserva[]    = [];
  reservasFiltradas: Reserva[]    = [];
  nombreAlojamiento  = new Map<number, string>();   // lodgingId → nombre
  cargando           = false;
  error              = '';

  // Filtro activo
  filtroEstado: EstadoReserva | 'TODAS' = 'TODAS';

  readonly FILTROS: { label: string; valor: EstadoReserva | 'TODAS' }[] = [
    { label: 'Todas',      valor: 'TODAS'      },
    { label: 'Confirmada', valor: 'CONFIRMADA' },
    { label: 'Pendiente',  valor: 'PENDIENTE'  },
    { label: 'Completada', valor: 'COMPLETADA' },
    { label: 'Cancelada',  valor: 'CANCELADA'  },
  ];

  // ── RESERV-10: Estado del modal de cancelación ────────────────
  mostrarModalCancelar: boolean       = false;
  reservaSeleccionada:  Reserva | null = null;  // reserva a cancelar
  cancelando:           boolean       = false;  // spinner mientras procesa
  errorCancelacion:     string        = '';     // error devuelto por el API

  private destroy$ = new Subject<void>();

  constructor(
    private reservaService:     ReservaService,
    private alojamientoService: AlojamientoService,
    private authService:        AuthService,
    public  router:             Router
  ) {}

  ngOnInit(): void {
    this.cargarReservas();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Carga de datos ────────────────────────────────────────────

  cargarReservas(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      this.error = 'No se pudo identificar al usuario.';
      return;
    }

    this.cargando = true;
    this.error    = '';

    this.reservaService.getByUser(usuario.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (reservas) => {
          // Ordenar por fecha de inicio descendente (más recientes primero)
          this.reservas = reservas.sort((a, b) =>
            b.startDate.localeCompare(a.startDate)
          );
          this.aplicarFiltro();
          this.cargando = false;
          this.cargarNombresAlojamientos(reservas);
        },
        error: (err) => {
          // 404 significa que el usuario no tiene reservas — no es un error real
          if (err.message?.includes('no encontr') || err.message?.includes('404')) {
            this.reservas          = [];
            this.reservasFiltradas = [];
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

  // ── Filtrado por estado ───────────────────────────────────────

  cambiarFiltro(estado: EstadoReserva | 'TODAS'): void {
    this.filtroEstado = estado;
    this.aplicarFiltro();
  }

  private aplicarFiltro(): void {
    this.reservasFiltradas = this.filtroEstado === 'TODAS'
      ? this.reservas
      : this.reservas.filter(r => r.status === this.filtroEstado);
  }

  // ── RESERV-10: Lógica de cancelación ─────────────────────────

  /**
   * Determina si una reserva puede cancelarse.
   * Solo CONFIRMADA y PENDIENTE son cancelables.
   */
  puedeCancelar(reserva: Reserva): boolean {
    return reserva.status === 'CONFIRMADA' || reserva.status === 'PENDIENTE';
  }

  /**
   * Abre el modal de cancelación para la reserva indicada.
   * Resetea el estado de error previo.
   */
  abrirModalCancelar(reserva: Reserva): void {
    this.reservaSeleccionada = reserva;
    this.errorCancelacion    = '';
    this.mostrarModalCancelar = true;
  }

  /** Cierra el modal sin cancelar la reserva. */
  cerrarModalCancelar(): void {
    if (this.cancelando) return;
    this.mostrarModalCancelar = false;
    this.reservaSeleccionada  = null;
    this.errorCancelacion     = '';
  }

  /**
   * Ejecuta la cancelación llamando al API.
   * Recibe el motivo emitido por el modal.
   * Al completarse actualiza el estado de la reserva localmente
   * sin recargar toda la lista.
   *
   * @param motivo Texto del motivo ingresado por el usuario
   */
  confirmarCancelacion(motivo: string): void {
    if (!this.reservaSeleccionada?.id || !motivo) return;

    this.cancelando       = true;
    this.errorCancelacion = '';

    this.reservaService.cancel(this.reservaSeleccionada.id, motivo)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Actualizar estado localmente para evitar recargar toda la lista
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
        },
        error: (err: Error) => {
          // El error se muestra dentro del modal para que el usuario
          // pueda leer el mensaje sin perder el contexto.
          this.cancelando       = false;
          this.errorCancelacion = err.message || 'No se pudo cancelar la reserva.';
        }
      });
  }

  // ── Helpers de template ───────────────────────────────────────

  contarPorEstado(estado: EstadoReserva | 'TODAS'): number {
    if (estado === 'TODAS') return this.reservas.length;
    return this.reservas.filter(r => r.status === estado).length;
  }

  getNombreAlojamiento(lodgingId: number): string {
    return this.nombreAlojamiento.get(lodgingId) ?? '...';
  }

  getEstadoLabel(status: EstadoReserva): string {
    return ESTADO_RESERVA_LABEL[status] ?? status;
  }

  getEstadoClass(status: EstadoReserva): string {
    return ESTADO_RESERVA_COLOR[status] ?? '';
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

  irAlDetalle(lodgingId: number): void {
    this.router.navigate(['/alojamientos', lodgingId]);
  }

  get totalReservas(): number {
    return this.reservasFiltradas.length;
  }

  get hayReservas(): boolean {
    return this.reservas.length > 0;
  }

  trackById(_: number, item: Reserva): number | undefined {
    return item.id;
  }
}
