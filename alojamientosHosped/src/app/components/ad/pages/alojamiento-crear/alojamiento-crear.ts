import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService }           from '../../../../../services/AlojamientoService';
import { AuthService }                  from '../../../../../services/AuthService';
import { AlojamientoServicioService }   from '../../../../../services/AlojamientoServicioService';
import { ImagenService }                from '../../../../../services/ImagenService';
import { ServicioDisponible }           from '../../../../models/servicio.model';
import { ImagenSubida }                 from '../../../../components/ad/molecules/image-uploader/image-uploader';

/**
 * AlojamientoCrearPageComponent — ALOJ-7 + ALOJ-10 + ALOJ-11
 *
 * ALOJ-11: Después de crear el alojamiento, guarda todas las imágenes
 *          subidas a Cloudinary en la tabla imagen de la BD.
 */
@Component({
  selector: 'app-alojamiento-crear',
  standalone: false,
  templateUrl: './alojamiento-crear.html',
  styleUrls: ['./alojamiento-crear.scss']
})
export class AlojamientoCrearPageComponent implements OnInit {

  form!: FormGroup;

  isSubmitting        = false;
  errorMessage        = '';
  successMessage      = '';
  hayImagenesSubiendo = false;

  // Lista completa de imágenes del uploader (para persistir en BD)
  private imagenesActuales: ImagenSubida[] = [];

  // ── ALOJ-10 ────────────────────────────────────────────────────
  serviciosDisponibles: ServicioDisponible[] = [];
  serviciosSeleccionados: Set<number>        = new Set();
  cargandoServicios                          = false;
  errorServicios                             = '';

  constructor(
    private fb:                         FormBuilder,
    private alojamientoService:         AlojamientoService,
    private alojamientoServicioService: AlojamientoServicioService,
    private imagenService:              ImagenService,
    private authService:                AuthService,
    private router:                     Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:          ['', [Validators.required, Validators.maxLength(150)]],
      description:   ['', [Validators.required]],
      address:       ['', [Validators.required, Validators.maxLength(255)]],
      city:          ['', [Validators.required, Validators.maxLength(100)]],
      pricePerNight: [null, [Validators.required, Validators.min(0.01)]],
      maxCapacity:   [null, [Validators.required, Validators.min(1)]],
      latitude:      [null, [Validators.required, Validators.min(-90),  Validators.max(90)]],
      longitude:     [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
      mainImage:     ['', [Validators.required]]
    });
    this.cargarServicios();
  }

  // ── ALOJ-11: callbacks del uploader ───────────────────────────

  onImagenPrincipalChange(url: string): void {
    this.form.patchValue({ mainImage: url });
    this.form.get('mainImage')?.markAsTouched();
  }

  onImagenesChange(imagenes: ImagenSubida[]): void {
    this.hayImagenesSubiendo  = imagenes.some(img => img.subiendo);
    this.imagenesActuales     = imagenes;
  }

  // ── ALOJ-10 ───────────────────────────────────────────────────

  cargarServicios(): void {
    this.cargandoServicios = true;
    this.alojamientoServicioService.getServiciosDisponibles().subscribe({
      next: (s) => { this.serviciosDisponibles = s; this.cargandoServicios = false; },
      error: () => { this.cargandoServicios = false; this.errorServicios = 'No se pudieron cargar los servicios.'; }
    });
  }

  toggleServicio(servicioId: number): void {
    const nuevos = new Set(this.serviciosSeleccionados);
    nuevos.has(servicioId) ? nuevos.delete(servicioId) : nuevos.add(servicioId);
    this.serviciosSeleccionados = nuevos;
  }

  isSeleccionado(servicioId: number): boolean {
    return this.serviciosSeleccionados.has(servicioId);
  }

  // ── Getters ───────────────────────────────────────────────────
  mostrarPreview = false;

  get datosPreview(): Partial<any> {
    return this.form?.value ?? {};
  }

  togglePreview(): void {
    this.mostrarPreview = !this.mostrarPreview;
  }

  get name()          { return this.form.get('name'); }
  get description()   { return this.form.get('description'); }
  get address()       { return this.form.get('address'); }
  get city()          { return this.form.get('city'); }
  get pricePerNight() { return this.form.get('pricePerNight'); }
  get maxCapacity()   { return this.form.get('maxCapacity'); }
  get latitude()      { return this.form.get('latitude'); }
  get longitude()     { return this.form.get('longitude'); }
  get mainImage()     { return this.form.get('mainImage'); }

  campoClase(control: AbstractControl | null): Record<string, boolean> {
    return {
      'border-red-400 bg-red-50':     !!control?.invalid && !!control?.touched,
      'border-green-400 bg-green-50': !!control?.valid   && !!control?.touched,
      'border-neutral-300':           !control?.touched
    };
  }

  // ── Envío ─────────────────────────────────────────────────────

  crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = !this.mainImage?.value
        ? 'Debes subir al menos una imagen del alojamiento.'
        : 'Por favor corrige los errores antes de continuar.';
      return;
    }
    if (this.hayImagenesSubiendo) {
      this.errorMessage = 'Espera a que terminen de subir todas las imágenes.';
      return;
    }

    this.isSubmitting   = true;
    this.errorMessage   = '';
    this.successMessage = '';

    const usuario = this.authService.getUsuario();
    const payload = { ...this.form.value, hostId: usuario?.id ?? 0, active: true };

    this.alojamientoService.create(payload).subscribe({
      next: (alojamientoCreado) => {
        const id = alojamientoCreado.id!;

        // Guardar imágenes en BD (todas las subidas a Cloudinary)
        const imagenesListas = this.imagenesActuales.filter(img => !img.subiendo && !img.error && img.url);
        const peticionesImg  = imagenesListas.map(img =>
          this.imagenService.crear(id, img.url, img.orden, img.nombre)
            .pipe(catchError(() => of(null)))
        );

        // Guardar servicios
        const serviciosIds   = Array.from(this.serviciosSeleccionados);
        const peticionesSvc  = serviciosIds.map(serviceId =>
          this.alojamientoServicioService.addServicio(id, serviceId)
            .pipe(catchError(() => of(null)))
        );

        const todasLasPeticiones = [...peticionesImg, ...peticionesSvc];

        if (todasLasPeticiones.length === 0) {
          this.finalizarCreacion();
          return;
        }

        forkJoin(todasLasPeticiones).subscribe({
          next: () => this.finalizarCreacion(),
          error: () => this.finalizarCreacion()
        });
      },
      error: (err: Error) => {
        this.isSubmitting = false;
        this.errorMessage = err.message || 'Error al crear el alojamiento.';
      }
    });
  }

  private finalizarCreacion(): void {
    this.isSubmitting   = false;
    this.successMessage = '¡Alojamiento creado exitosamente!';
    setTimeout(() => this.router.navigate(['/alojamientos']), 1500);
  }

  cancelar(): void {
    this.router.navigate(['/alojamientos']);
  }
}