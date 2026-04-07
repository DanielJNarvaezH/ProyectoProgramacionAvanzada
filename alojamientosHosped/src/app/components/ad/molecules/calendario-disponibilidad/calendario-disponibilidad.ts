import {
  Component, Input, OnInit, OnChanges,
  SimpleChanges, Output, EventEmitter, OnDestroy
} from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { catchError, of } from 'rxjs';
import { ReservaService } from '../../../../../services/ReservaService';
import { Reserva } from '../../../../models/reserva.model';

/**
 * CalendarioDisponibilidadComponent — RESERV-3 / RESERV-5
 *
 * Muestra un calendario interactivo donde el usuario puede:
 * - Ver fechas bloqueadas (ya reservadas) en rojo
 * - Ver fechas disponibles en verde
 * - Seleccionar rango de fechas para una nueva reserva
 *
 * RESERV-5: Validaciones de fechas en frontend:
 * - No se pueden seleccionar fechas pasadas (check-in ni check-out)
 * - check-out debe ser estrictamente posterior al check-in (mín. 1 noche)
 * - Si el usuario selecciona el mismo día dos veces se muestra error claro
 * - Si el rango reordenado deja el check-in en el pasado se corrige con error
 *
 * Uso:
 *   <app-calendario-disponibilidad
 *     [lodgingId]="alojamiento.id"
 *     (rangoSeleccionado)="onRango($event)">
 *   </app-calendario-disponibilidad>
 */
@Component({
  selector: 'app-calendario-disponibilidad',
  standalone: false,
  templateUrl: './calendario-disponibilidad.html',
  styleUrls: ['./calendario-disponibilidad.scss']
})
export class CalendarioDisponibilidadComponent implements OnInit, OnChanges, OnDestroy {

  @Input() lodgingId!: number;

  /** Emite { startDate, endDate } en formato yyyy-MM-dd cuando el usuario selecciona un rango */
  @Output() rangoSeleccionado = new EventEmitter<{ startDate: string; endDate: string }>();

  /** Emite cuando el usuario cancela/limpia la selección del calendario */
  @Output() rangoCancelado = new EventEmitter<void>();

  // Estado
  cargando     = false;
  error        = '';
  reservas:    Reserva[] = [];

  // Fechas bloqueadas (Set de strings yyyy-MM-dd)
  fechasBloqueadas = new Set<string>();

  // Selección del usuario
  fechaInicio: Date | null = null;
  fechaFin:    Date | null = null;
  mesActual:   Date = new Date();

  // Grid del calendario
  diasDelMes:  (Date | null)[] = [];
  readonly DIAS_SEMANA = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
  readonly MESES = [
    'Enero','Febrero','Marzo','Abril','Mayo','Junio',
    'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'
  ];

  private destroy$ = new Subject<void>();

  constructor(private reservaService: ReservaService) {}

  ngOnInit(): void {
    this.generarDiasDelMes();
    if (this.lodgingId) this.cargarReservas();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lodgingId'] && !changes['lodgingId'].firstChange) {
      this.cargarReservas();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Carga de reservas ─────────────────────────────────────────

  private cargarReservas(): void {
    this.cargando = true;
    this.error    = '';

    this.reservaService.getByAlojamiento(this.lodgingId)
      .pipe(
        takeUntil(this.destroy$),
        catchError(() => of([] as Reserva[]))
      )
      .subscribe(reservas => {
        this.reservas = reservas.filter(
          r => r.status === 'CONFIRMADA' || r.status === 'PENDIENTE'
        );
        this.construirFechasBloqueadas();
        this.cargando = false;
      });
  }

  private construirFechasBloqueadas(): void {
    this.fechasBloqueadas.clear();
    this.reservas.forEach(r => {
      const inicio = new Date(r.startDate + 'T00:00:00');
      const fin    = new Date(r.endDate   + 'T00:00:00');
      const cursor = new Date(inicio);
      while (cursor <= fin) {
        this.fechasBloqueadas.add(this.toYYYYMMDD(cursor));
        cursor.setDate(cursor.getDate() + 1);
      }
    });
  }

  // ── Generación del grid del mes ───────────────────────────────

  generarDiasDelMes(): void {
    const año  = this.mesActual.getFullYear();
    const mes  = this.mesActual.getMonth();
    const primer = new Date(año, mes, 1);
    const ultimo = new Date(año, mes + 1, 0);
    const dias: (Date | null)[] = [];

    // Relleno inicial (días vacíos antes del primer día)
    for (let i = 0; i < primer.getDay(); i++) dias.push(null);

    // Días del mes
    for (let d = 1; d <= ultimo.getDate(); d++) {
      dias.push(new Date(año, mes, d));
    }

    this.diasDelMes = dias;
  }

  // ── Navegación entre meses ────────────────────────────────────

  mesSiguiente(): void {
    this.mesActual = new Date(
      this.mesActual.getFullYear(),
      this.mesActual.getMonth() + 1,
      1
    );
    this.generarDiasDelMes();
  }

  mesAnterior(): void {
    const hoy = new Date();
    const anterior = new Date(
      this.mesActual.getFullYear(),
      this.mesActual.getMonth() - 1,
      1
    );
    // No permitir navegar a meses pasados
    if (anterior.getFullYear() < hoy.getFullYear() ||
       (anterior.getFullYear() === hoy.getFullYear() &&
        anterior.getMonth() < hoy.getMonth())) return;
    this.mesActual = anterior;
    this.generarDiasDelMes();
  }

  get puedeIrAtras(): boolean {
    const hoy = new Date();
    return !(this.mesActual.getFullYear() === hoy.getFullYear() &&
             this.mesActual.getMonth()    === hoy.getMonth());
  }

  // ── Selección de fechas ───────────────────────────────────────

  seleccionarDia(dia: Date | null): void {
    if (!dia || this.esBloqueado(dia) || this.esPasado(dia)) return;

    if (!this.fechaInicio || (this.fechaInicio && this.fechaFin)) {
      // Primera selección o reinicio — solo check-in
      this.fechaInicio = dia;
      this.fechaFin    = null;
      this.error       = '';
    } else {
      // Segunda selección — check-out

      // RESERV-5: check-out debe ser estrictamente posterior al check-in (mín. 1 noche)
      if (this.toYYYYMMDD(dia) === this.toYYYYMMDD(this.fechaInicio)) {
        this.error = 'El check-out debe ser un día diferente al check-in (mínimo 1 noche).';
        return;
      }

      // Ordenar el rango si el usuario seleccionó en orden inverso
      let inicio: Date;
      let fin:    Date;
      if (dia < this.fechaInicio) {
        inicio = dia;
        fin    = this.fechaInicio;
      } else {
        inicio = this.fechaInicio;
        fin    = dia;
      }

      // RESERV-5: verificar que check-in no sea pasado (edge case al reordenar)
      if (this.esPasado(inicio)) {
        this.error       = 'La fecha de check-in no puede ser anterior a hoy.';
        this.fechaInicio = dia;
        this.fechaFin    = null;
        return;
      }

      // Verificar que no haya fechas bloqueadas dentro del rango
      if (this.hayBloqueadosEnRango(inicio, fin)) {
        this.error       = 'El rango seleccionado incluye fechas no disponibles.';
        this.fechaInicio = dia;
        this.fechaFin    = null;
        return;
      }

      // Rango válido
      this.fechaInicio = inicio;
      this.fechaFin    = fin;
      this.error       = '';
      this.rangoSeleccionado.emit({
        startDate: this.toYYYYMMDD(this.fechaInicio),
        endDate:   this.toYYYYMMDD(this.fechaFin)
      });
    }
  }

  limpiarSeleccion(): void {
    this.fechaInicio = null;
    this.fechaFin    = null;
    this.error       = '';
    this.rangoCancelado.emit();
  }

  // ── Helpers de estado de día ──────────────────────────────────

  esBloqueado(dia: Date): boolean {
    return this.fechasBloqueadas.has(this.toYYYYMMDD(dia));
  }

  esPasado(dia: Date): boolean {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return dia < hoy;
  }

  esInicio(dia: Date): boolean {
    return !!this.fechaInicio && this.toYYYYMMDD(dia) === this.toYYYYMMDD(this.fechaInicio);
  }

  esFin(dia: Date): boolean {
    return !!this.fechaFin && this.toYYYYMMDD(dia) === this.toYYYYMMDD(this.fechaFin);
  }

  enRango(dia: Date): boolean {
    if (!this.fechaInicio || !this.fechaFin) return false;
    return dia > this.fechaInicio && dia < this.fechaFin;
  }

  esHoy(dia: Date): boolean {
    return this.toYYYYMMDD(dia) === this.toYYYYMMDD(new Date());
  }

  private hayBloqueadosEnRango(inicio: Date, fin: Date): boolean {
    const cursor = new Date(inicio);
    cursor.setDate(cursor.getDate() + 1);
    while (cursor < fin) {
      if (this.fechasBloqueadas.has(this.toYYYYMMDD(cursor))) return true;
      cursor.setDate(cursor.getDate() + 1);
    }
    return false;
  }

  // ── Getters de template ───────────────────────────────────────

  get tituloMes(): string {
    return `${this.MESES[this.mesActual.getMonth()]} ${this.mesActual.getFullYear()}`;
  }

  get noches(): number {
    if (!this.fechaInicio || !this.fechaFin) return 0;
    const diff = this.fechaFin.getTime() - this.fechaInicio.getTime();
    return Math.round(diff / (1000 * 60 * 60 * 24));
  }

  get rangoLabel(): string {
    if (!this.fechaInicio) return '';
    const inicio = this.formatearFecha(this.fechaInicio);
    if (!this.fechaFin) return `Desde ${inicio}`;
    return `${inicio} → ${this.formatearFecha(this.fechaFin)}`;
  }

  private formatearFecha(fecha: Date): string {
    return fecha.toLocaleDateString('es-CO', {
      day: 'numeric', month: 'short', year: 'numeric'
    });
  }

  private toYYYYMMDD(fecha: Date): string {
    const y = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, '0');
    const d = String(fecha.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}