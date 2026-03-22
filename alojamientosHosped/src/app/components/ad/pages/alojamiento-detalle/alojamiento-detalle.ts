import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AlojamientoService }        from '../../../../../services/AlojamientoService';
import { ImagenService }              from '../../../../../services/ImagenService';
import { ComentarioService }          from '../../../../../services/ComentarioService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { AuthService }                from '../../../../../services/AuthService';

import { Alojamiento }         from '../../../../models/alojamiento.model';
import { Imagen }              from '../../../../models/imagen.model';
import { Comentario }          from '../../../../models/comentario.model';
import { AlojamientoServicio } from '../../../../models/alojamiento-servicio.model';

/**
 * AlojamientoDetallePageComponent — ALOJ-5 + ALOJ-8 + nav prev/next + ALOJ-13
 *
 * ALOJ-13: Botón Eliminar con modal de confirmación visible solo
 * para el anfitrión dueño del alojamiento. Tras eliminar redirige
 * al listado y actualiza la lista (el alojamiento queda inactivo).
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

  private destroy$ = new Subject<void>();

  constructor(
    private route:                    ActivatedRoute,
    private router:                   Router,
    private alojamientoService:       AlojamientoService,
    private imagenService:            ImagenService,
    private comentarioService:        ComentarioService,
    private alojamientoServicioSvc:   AlojamientoServicioService,
    private authService:              AuthService
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
    // Fix: pasar origen para que cancelar en editar vuelva al detalle
    this.router.navigate(
      ['/alojamientos', this.alojamiento!.id, 'editar'],
      { queryParams: { origen: `/alojamientos/${this.alojamiento!.id}` } }
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
    const { latitude: lat, longitude: lng } = this.alojamiento;
    if (lat && lng) {
      this.mapaUrl =
        `https://www.openstreetmap.org/export/embed.html` +
        `?bbox=${lng - 0.01},${lat - 0.01},${lng + 0.01},${lat + 0.01}` +
        `&layer=mapnik&marker=${lat},${lng}`;
    }
  }

  volver(): void { this.router.navigate([this.origen]); }

  get precioFormateado(): string {
    return this.alojamiento?.pricePerNight
      ? this.alojamiento.pricePerNight.toLocaleString('es-CO') : '0';
  }

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