import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService }        from '../../../../../services/AuthService';
import { ImagenService }      from '../../../../../services/ImagenService';
import { Alojamiento }        from '../../../../models/alojamiento.model';
import { Imagen }             from '../../../../models/imagen.model';
import { ImagenSubida, ImagenExistente } from '../../../../components/ad/molecules/image-uploader/image-uploader';

/**
 * AlojamientoEditarPageComponent â€” ALOJ-8 + ALOJ-11
 *
 * ALOJ-11: Precarga las imÃ¡genes existentes en el uploader.
 *          Al guardar sincroniza: elimina las que se borraron,
 *          agrega las nuevas subidas a Cloudinary.
 */
@Component({
  selector: 'app-alojamiento-editar',
  standalone: false,
  templateUrl: './alojamiento-editar.html',
  styleUrls: ['./alojamiento-editar.scss']
})
export class AlojamientoEditarPageComponent implements OnInit, OnDestroy {

  form!: FormGroup;
  alojamientoId!: number;
  private origen = ''; // Fix: para volver al anterior al cancelar

  cargando          = true;
  guardando         = false;
  errorCarga        = '';
  errorGuardar      = '';
  exito             = false;
  hayImagenesSubiendo = false;

  // ALOJ-11: imÃ¡genes existentes para precargar en el uploader
  imagenesExistentes: ImagenExistente[] = [];

  // Estado actual del uploader
  private imagenesActuales: ImagenSubida[] = [];

  // ImÃ¡genes originales de BD para comparar al guardar
  private imagenesBD: Imagen[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private fb:                 FormBuilder,
    private route:              ActivatedRoute,
    private router:             Router,
    private alojamientoService: AlojamientoService,
    private authService:        AuthService,
    private imagenService:      ImagenService
  ) {}

  mostrarPreview = false;

  get datosPreview(): Partial<any> {
    return this.form?.value ?? {};
  }

  togglePreview(): void {
    this.mostrarPreview = !this.mostrarPreview;
  }

  ngOnInit(): void {
    this.alojamientoId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.alojamientoId) {
      this.router.navigate(['/alojamientos']);
      return;
    }
    // Fix: guardar origen para cancelar vuelva al anterior
    this.origen = this.route.snapshot.queryParamMap.get('origen')
      || `/alojamientos/${this.alojamientoId}`;
    this.inicializarForm();
    this.cargarAlojamiento();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private inicializarForm(): void {
    this.form = this.fb.group({
      name:          ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description:   ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      address:       ['', [Validators.required, Validators.maxLength(200)]],
      city:          ['', [Validators.required, Validators.maxLength(100)]],
      latitude:      [null, [Validators.required, Validators.min(-90),  Validators.max(90)]],
      longitude:     [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
      pricePerNight: [null, [Validators.required, Validators.min(1)]],
      maxCapacity:   [null, [Validators.required, Validators.min(1), Validators.max(50)]],
      mainImage:     [''],
      active:        [true]
    });
  }

  private cargarAlojamiento(): void {
    this.cargando   = true;
    this.errorCarga = '';

    // Cargar alojamiento e imÃ¡genes en paralelo
    forkJoin({
      alojamiento: this.alojamientoService.getById(this.alojamientoId),
      imagenes:    this.imagenService.getByAlojamiento(this.alojamientoId)
        .pipe(catchError(() => of([] as Imagen[])),
      map(result => result ?? []) 
      )
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ alojamiento, imagenes }) => {
          this.form.patchValue({
            name:          alojamiento.name,
            description:   alojamiento.description,
            address:       alojamiento.address,
            city:          alojamiento.city,
            latitude:      alojamiento.latitude,
            longitude:     alojamiento.longitude,
            pricePerNight: alojamiento.pricePerNight,
            maxCapacity:   alojamiento.maxCapacity,
            mainImage:     alojamiento.mainImage ?? '',
            active:        alojamiento.active ?? true
          });

          // Guardar referencia de BD para comparar al guardar
          this.imagenesBD = imagenes;

          // Construir lista para precargar en el uploader
          // Si hay imÃ¡genes en tabla imagen, usarlas; si no, usar mainImage
          if (imagenes.length > 0) {
            this.imagenesExistentes = imagenes.map(img => ({
              id:    img.id!,
              url:   img.url,
              orden: img.ordenVisualizacion ?? 0,
              nombre: `imagen_${img.id}`
            }));
          } else if (alojamiento.mainImage) {
            // Compatibilidad: alojamientos sin tabla imagen aÃºn
            this.imagenesExistentes = [{
              id:    0,
              url:   alojamiento.mainImage,
              orden: 0,
              nombre: 'imagen_principal'
            }];
          }

          this.cargando = false;
        },
        error: (err) => {
          this.errorCarga = err.message || 'No se pudo cargar el alojamiento.';
          this.cargando   = false;
        }
      });
  }

  // â”€â”€ ALOJ-11: callbacks del uploader â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  onImagenPrincipalChange(url: string): void {
    this.form.patchValue({ mainImage: url });
  }

  onImagenesChange(imagenes: ImagenSubida[]): void {
    this.hayImagenesSubiendo = imagenes.some(img => img.subiendo);
    this.imagenesActuales    = imagenes;
  }

  // â”€â”€ Guardar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.hayImagenesSubiendo) {
      this.errorGuardar = 'Espera a que terminen de subir todas las imÃ¡genes.';
      return;
    }

    this.guardando    = true;
    this.errorGuardar = '';
    this.exito        = false;

    const usuario = this.authService.getUsuario();
    const payload: Alojamiento = { ...this.form.value, hostId: usuario?.id ?? 0 };

    this.alojamientoService.update(this.alojamientoId, payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.sincronizarImagenes(),
        error: (err) => {
          this.guardando    = false;
          this.errorGuardar = err.message || 'Error al guardar los cambios.';
        }
      });
  }

  /**
   * ALOJ-11: Sincroniza imÃ¡genes en BD:
   * - Elimina las que estaban en BD y ya no estÃ¡n en el uploader
   * - Agrega las nuevas (las que no tienen bdId)
   */
  private sincronizarImagenes(): void {
    const idsBD       = this.imagenesBD.map(img => img.id!);
    const idsActuales = this.imagenesActuales
      .filter(img => img.bdId)
      .map(img => img.bdId!);

    // ImÃ¡genes a eliminar: estaban en BD pero ya no estÃ¡n en el uploader
    const aEliminar = idsBD.filter(id => !idsActuales.includes(id));

    // ImÃ¡genes a agregar: nuevas (sin bdId, ya subidas a Cloudinary)
    const aAgregar = this.imagenesActuales.filter(
      img => !img.bdId && !img.subiendo && !img.error && img.url
    );

    const peticionesEliminar = aEliminar.map(id =>
      this.imagenService.eliminar(id).pipe(catchError(() => of(null)))
    );

    const peticionesAgregar = aAgregar.map(img =>
      this.imagenService.crear(this.alojamientoId, img.url, img.orden, img.nombre)
        .pipe(catchError(() => of(null)))
    );

    const todas = [...peticionesEliminar, ...peticionesAgregar];

    if (todas.length === 0) {
      this.finalizarGuardado();
      return;
    }

    forkJoin(todas).subscribe({
      next: () => this.finalizarGuardado(),
      error: () => this.finalizarGuardado()
    });
  }

  private finalizarGuardado(): void {
    this.guardando = false;
    this.exito     = true;
    setTimeout(() => this.router.navigate(['/alojamientos', this.alojamientoId]), 1800);
  }

  cancelar(): void {
    // Fix: volver al inmediatamente anterior
    this.router.navigate([this.origen]);
  }

  campo(name: string) { return this.form.get(name); }

  invalido(name: string): boolean {
    const c = this.campo(name);
    return !!(c && c.invalid && c.touched);
  }

  errorMsg(name: string): string {
    const c = this.campo(name);
    if (!c || !c.errors) return '';
    if (c.errors['required'])  return 'Este campo es obligatorio.';
    if (c.errors['minlength']) return `MÃ­nimo ${c.errors['minlength'].requiredLength} caracteres.`;
    if (c.errors['maxlength']) return `MÃ¡ximo ${c.errors['maxlength'].requiredLength} caracteres.`;
    if (c.errors['min'])       return `El valor mÃ­nimo es ${c.errors['min'].min}.`;
    if (c.errors['max'])       return `El valor mÃ¡ximo es ${c.errors['max'].max}.`;
    return 'Valor invÃ¡lido.';
  }
}