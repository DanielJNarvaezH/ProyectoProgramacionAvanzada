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
 * AlojamientoDetallePageComponent — ALOJ-5 + ALOJ-8
 *
 * Vista completa del detalle de un alojamiento.
 * ALOJ-8: muestra botón Editar solo si el usuario logueado es el dueño.
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

  mapaUrl = '';

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
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/alojamientos']);
      return;
    }
    this.cargarDetalle(id);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-8: getter que indica si el usuario logueado es el dueño
  // ─────────────────────────────────────────────────────────────────

  get esAnfitrionDueno(): boolean {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id || !this.alojamiento?.hostId) return false;
    return usuario.id === this.alojamiento.hostId;
  }

  // ─────────────────────────────────────────────────────────────────
  // ALOJ-8: navegar al formulario de edición
  // ─────────────────────────────────────────────────────────────────

  irAEditar(): void {
    this.router.navigate(['/alojamientos', this.alojamiento!.id, 'editar']);
  }

  // ─────────────────────────────────────────────────────────────────
  // Carga de datos
  // ─────────────────────────────────────────────────────────────────

  cargarDetalle(id: number): void {
    this.cargando = true;
    this.error    = '';

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

  volver(): void {
    this.router.navigate(['/alojamientos']);
  }

  get precioFormateado(): string {
    return this.alojamiento?.pricePerNight
      ? this.alojamiento.pricePerNight.toLocaleString('es-CO')
      : '0';
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

  get hayMapa(): boolean {
    return !!this.mapaUrl;
  }
}