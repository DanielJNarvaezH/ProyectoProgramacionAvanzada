import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, forkJoin, of, takeUntil, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AlojamientoService }        from '../../../../../services/AlojamientoService';
import { ImagenService }              from '../../../../../services/ImagenService';
import { ComentarioService }          from '../../../../../services/ComentarioService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { AuthService }                from '../../../../../services/AuthService';
import { MapService }                 from '../../../../../services/MapService';
import { FavoritoService }            from '../../../../../services/FavoritoService'; // ALOJ-21
import { ReservaService }             from '../../../../../services/ReservaService';  // RESERV-4

import { Alojamiento, Imagen, Comentario, AlojamientoServicio } from '../../../../models';

/**
 * AlojamientoDetallePageComponent — ALOJ-5 + ALOJ-8 + nav prev/next + ALOJ-13 + ALOJ-21
 *
 * ALOJ-13: Botón Eliminar con modal de confirmación visible solo
 * para el anfitrión dueño del alojamiento. Tras eliminar redirige
 * al listado y actualiza la lista (el alojamiento queda inactivo).
 *
 * ALOJ-21: Botón de favorito (corazón) visible para usuarios USUARIO.
 * Verifica el estado al cargar y permite agregar/quitar con un clic.
 */
@Component({
  selector: 'app-alojamiento-detalle',
  standalone: false,
  templateUrl: './alojamiento-detalle.html',
  styleUrls: ['./alojamiento-detalle.scss']
})
export class AlojamientoDetallePageComponent implements OnInit, OnDestroy {

  cargando = true;
  error    = '';

  alojamiento: Alojamiento | null    = null;
  imagenes:    Imagen[]              = [];
  servicios:   AlojamientoServicio[] = [];
  comentarios: Comentario[]          = [];
  promedio     = 0;
  mapaUrl      = '';

  // ── Navegación prev/next ─────────────────────────────────────────
  private idsContexto: number[] = [];
  origen = '/alojamientos'; // Fix: destino del botón Volver

  // ── ALOJ-13: Modal de eliminación ────────────────────────────────
  mostrarModalEliminar = false;
  eliminando           = false;
  errorEliminacion     = '';

  // ── ALOJ-21: Favoritos ────────────────────────────────────────────
  esFavorito       = false;
  toggleandoFav    = false;

  private destroy$ = new Subject<void>();

  constructor(
    private route:                    ActivatedRoute,
    public  router:                   Router,
    private alojamientoService:       AlojamientoService,
    private imagenService:            ImagenService,
    private comentarioService:        ComentarioService,
    private alojamientoServicioSvc:   AlojamientoServicioService,
    private authService:              AuthService,
    private mapService:               MapService,
    private favoritoService:          FavoritoService,  // ALOJ-21
    private reservaService:           ReservaService    // RESERV-4
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = Number(params.get('id'));
        if (!id) {
          this.router.navigate(['/alojamientos']);
          return;
        }
        const idsParam = this.route.snapshot.queryParamMap.get('ids');
        this.idsContexto = idsParam
          ? idsParam.split(',').map(Number).filter(n => !isNaN(n) && n > 0)
          : [];
        const origenParam = this.route.snapshot.queryParamMap.get('origen');
        if (origenParam) this.origen = origenParam;
        this.cargarDetalle(id);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  rangoReserva: { startDate: string; endDate: string } | null = null;

  // ── RESERV-4: Formulario de solicitud de reserva ──────────────
  numGuests:       number = 1;      // número de huéspedes seleccionado
  enviandoReserva: boolean = false; // spinner del botón Reservar
  errorReserva:    string  = '';    // mensaje de error al reservar
  reservaExitosa:  boolean = false; // confirmación tras reservar

  onRangoSeleccionado(rango: { startDate: string; endDate: string }): void {
    this.rangoReserva   = rango;
    this.errorReserva   = '';
    this.reservaExitosa = false;
  }

  onRangoCancelado(): void {
    this.rangoReserva   = null;
    this.errorReserva   = '';
    this.reservaExitosa = false;
  }

  // ── RESERV-4: Cálculo de precio en tiempo real ────────────────

  /** Número de noches entre las fechas seleccionadas */
  get noches(): number {
    if (!this.rangoReserva) return 0;
    const inicio = new Date(this.rangoReserva.startDate + 'T00:00:00');
    const fin    = new Date(this.rangoReserva.endDate   + 'T00:00:00');
    const diff   = fin.getTime() - inicio.getTime();
    return Math.max(0, Math.round(diff / (1000 * 60 * 60 * 24)));
  }

  /** Precio total calculado en tiempo real */
  get precioTotal(): number {
    if (!this.alojamiento || this.noches === 0) return 0;
    return this.noches * this.alojamiento.pricePerNight;
  }

  /** Validaciones del formulario antes de enviar */
  get formularioValido(): boolean {
    return !!this.rangoReserva
      && this.noches >= 1
      && !!this.alojamiento
      && this.numGuests >= 1
      && this.numGuests <= this.alojamiento.maxCapacity;
  }

  /** Enviar la solicitud de reserva — RESERV-4 */
  solicitarReserva(): void {
    if (!this.formularioValido || !this.alojamiento || !this.rangoReserva) return;

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) return;

    this.enviandoReserva = true;
    this.errorReserva    = '';
    this.reservaExitosa  = false;

    this.reservaService.create({
      guestId:    usuario.id,
      lodgingId:  this.alojamiento.id!,
      startDate:  this.rangoReserva.startDate,
      endDate:    this.rangoReserva.endDate,
      numGuests:  this.numGuests,
      totalPrice: this.precioTotal,
      status:     'CONFIRMADA'
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.enviandoReserva = false;
          this.reservaExitosa  = true;
          this.rangoReserva    = null;
          this.numGuests       = 1;
        },
        error: (err: Error) => {
          this.enviandoReserva = false;
          this.errorReserva    = err.message || 'No se pudo completar la reserva.';
        }
      });
  }

  // ── Navegación prev/next ─────────────────────────────────────────

  get idActual(): number { return this.alojamiento?.id ?? 0; }
  get indexActual(): number { return this.idsContexto.indexOf(this.idActual); }

  get idAnterior(): number | null {
    if (this.idsContexto.length < 2 || this.indexActual <= 0) return null;
    return this.idsContexto[this.indexActual - 1];
  }

  get idSiguiente(): number | null {
    if (this.idsContexto.length < 2 || this.indexActual >= this.idsContexto.length - 1) return null;
    return this.idsContexto[this.indexActual + 1];
  }

  get hayNavegacion(): boolean { return this.idsContexto.length > 1; }

  get posicionLabel(): string {
    if (!this.hayNavegacion) return '';
    return `${this.indexActual + 1} / ${this.idsContexto.length}`;
  }

  irAnterior(): void { if (this.idAnterior) this.navegar(this.idAnterior); }
  irSiguiente(): void { if (this.idSiguiente) this.navegar(this.idSiguiente); }

  private navegar(id: number): void {
    const queryParams = this.idsContexto.length > 1 ? { ids: this.idsContexto.join(',') } : {};
    this.router.navigate(['/alojamientos', id], { queryParams });
  }

  // ── ALOJ-8 ───────────────────────────────────────────────────────

  get esAnfitrionDueno(): boolean {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id || !this.alojamiento?.hostId) return false;
    return usuario.id === this.alojamiento.hostId;
  }

  irAEditar(): void {
    // Fix nav: pasar origen (URL del detalle) + origenDetalle (de dónde vino el usuario
    // al detalle, p.ej. /mis-alojamientos) para que al volver del editar el detalle
    // restaure tanto las flechas prev/next como el destino correcto del botón Volver.
    const queryParams: Record<string, string> = {
      origen:        `/alojamientos/${this.alojamiento!.id}`,
      origenDetalle: this.origen   // preserva /mis-alojamientos o /alojamientos
    };
    if (this.idsContexto.length > 1) {
      queryParams['ids'] = this.idsContexto.join(',');
    }
    this.router.navigate(
      ['/alojamientos', this.alojamiento!.id, 'editar'],
      { queryParams }
    );
  }

  // ── ALOJ-13: Eliminación ─────────────────────────────────────────

  abrirModalEliminar(): void {
    this.errorEliminacion    = '';
    this.mostrarModalEliminar = true;
  }

  cerrarModalEliminar(): void {
    if (this.eliminando) return;
    this.mostrarModalEliminar = false;
    this.errorEliminacion    = '';
  }

  confirmarEliminar(): void {
    if (!this.alojamiento?.id) return;

    this.eliminando       = true;
    this.errorEliminacion = '';

    this.alojamientoService.delete(this.alojamiento.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.eliminando           = false;
          this.mostrarModalEliminar = false;
          // Redirigir al panel del anfitrión o al listado
          this.router.navigate(['/mis-alojamientos']);
        },
        error: (err: Error) => {
          this.eliminando       = false;
          this.errorEliminacion = err.message || 'No se pudo eliminar el alojamiento.';
        }
      });
  }

  // ── Carga de datos ───────────────────────────────────────────────

  cargarDetalle(id: number): void {
    this.cargando    = true;
    this.error       = '';
    this.alojamiento = null;

    this.alojamientoService.getById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (aloj) => {
          this.alojamiento = aloj;
          this.construirMapaUrl();
          this.cargarDatosSoporte(id);
          // ALOJ-21: verificar si el usuario ya marcó este alojamiento como favorito
          this.verificarFavorito(id);
        },
        error: (err) => {
          this.error    = err.message || 'No se pudo cargar el alojamiento';
          this.cargando = false;
        }
      });
  }

  private cargarDatosSoporte(id: number): void {
    forkJoin({
      imagenes:    this.imagenService.getByAlojamiento(id).pipe(catchError(() => of([] as Imagen[]))),
      servicios:   this.alojamientoServicioSvc.getServiciosByAlojamiento(id).pipe(catchError(() => of([] as AlojamientoServicio[]))),
      comentarios: this.comentarioService.getByAlojamiento(id).pipe(catchError(() => of([] as Comentario[]))),
      promedio:    this.comentarioService.getPromedio(id).pipe(catchError(() => of(0)))
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ imagenes, servicios, comentarios, promedio }) => {
          this.imagenes    = imagenes;
          this.servicios   = servicios;
          this.comentarios = comentarios;
          this.promedio    = promedio ?? 0;
          this.cargando    = false;
        },
        error: () => { this.cargando = false; }
      });
  }

  private construirMapaUrl(): void {
    if (!this.alojamiento) return;
    this.mapaUrl = this.mapService.buildEmbedUrl(
      this.alojamiento.latitude,
      this.alojamiento.longitude
    );
  }

  // ── ALOJ-21: Favoritos ────────────────────────────────────────────

  /** Solo los usuarios con rol USUARIO pueden marcar favoritos */
  get puedeMarcarFavorito(): boolean {
    return this.authService.getUsuario()?.role === 'USUARIO';
  }

  /** Solo los usuarios con rol USUARIO ven el calendario y el botón de reservar */
  get puedeReservar(): boolean {
    return this.authService.getUsuario()?.role === 'USUARIO';
  }

  /** Consulta al backend si este alojamiento ya es favorito del usuario */
  private verificarFavorito(alojamientoId: number): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id || !this.puedeMarcarFavorito) return;

    this.favoritoService.esFavorito(usuario.id, alojamientoId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resultado) => { this.esFavorito = resultado; },
        error: ()         => { this.esFavorito = false; }
      });
  }

  /** Agrega o quita el alojamiento de favoritos */
  toggleFavorito(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id || !this.alojamiento?.id || this.toggleandoFav) return;

    this.toggleandoFav = true;
    const alojId = this.alojamiento.id;
    const userId = usuario.id;

    const accion$: Observable<unknown> = this.esFavorito
      ? this.favoritoService.eliminar(userId, alojId)
      : this.favoritoService.agregar(userId, alojId);

    accion$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.esFavorito    = !this.esFavorito;
        this.toggleandoFav = false;
      },
      error: () => { this.toggleandoFav = false; }
    });
  }

  volver(): void { this.router.navigate([this.origen]); }

  get estrellas(): Array<'full' | 'half' | 'empty'> {
    return Array.from({ length: 5 }, (_, i) => {
      const pos = i + 1;
      if (this.promedio >= pos)       return 'full';
      if (this.promedio >= pos - 0.5) return 'half';
      return 'empty';
    });
  }

  get promedioLabel(): string {
    return this.comentarios.length > 0 ? this.promedio.toFixed(1) : 'Nuevo';
  }

  get totalResenasLabel(): string {
    const n = this.comentarios.length;
    if (n === 0) return 'Sin reseñas';
    return n === 1 ? '1 reseña' : `${n} reseñas`;
  }

  get hayMapa(): boolean { return !!this.mapaUrl; }
}