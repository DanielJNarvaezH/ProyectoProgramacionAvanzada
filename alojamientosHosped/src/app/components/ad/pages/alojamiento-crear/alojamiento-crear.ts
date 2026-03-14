import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService }           from '../../../../../services/AlojamientoService';
import { AuthService }                  from '../../../../../services/AuthService';
import { AlojamientoServicioService }   from '../../../../../services/AlojamientoServicioService';
import { ServicioDisponible }           from '../../../../models/servicio.model';

/**
 * AlojamientoCrearPageComponent — ALOJ-7 + ALOJ-10
 *
 * Formulario de creación de alojamiento con:
 * ALOJ-7:  Reactive form con todos los campos y validaciones en tiempo real
 *          Acceso restringido a ANFITRION (protegido por anfitrionGuard en routing)
 * ALOJ-10: Selector de servicios con checkboxes
 *
 * Validaciones implementadas:
 * - nombre: requerido, max 150 chars
 * - descripción: requerida
 * - dirección: requerida, max 255 chars
 * - ciudad: requerida, max 100 chars
 * - precio: requerido, > 0
 * - capacidad: requerida, >= 1
 * - latitud: requerida, -90 a 90
 * - longitud: requerida, -180 a 180
 * - imagen: requerida, URL válida http/https
 */
@Component({
  selector: 'app-alojamiento-crear',
  standalone: false,
  templateUrl: './alojamiento-crear.html',
  styleUrls: ['./alojamiento-crear.scss']
})
export class AlojamientoCrearPageComponent implements OnInit {

  form!: FormGroup;

  isSubmitting   = false;
  errorMessage   = '';
  successMessage = '';

  // ── ALOJ-10: Estado de servicios ──────────────────────────────
  serviciosDisponibles: ServicioDisponible[] = [];
  serviciosSeleccionados: Set<number>        = new Set();
  cargandoServicios                          = false;
  errorServicios                             = '';

  constructor(
    private fb:                         FormBuilder,
    private alojamientoService:         AlojamientoService,
    private alojamientoServicioService: AlojamientoServicioService,
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
      mainImage:     ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]]
    });

    this.cargarServicios();
  }

  // ── ALOJ-10: Cargar servicios del backend ─────────────────────

  cargarServicios(): void {
    this.cargandoServicios = true;
    this.errorServicios    = '';

    this.alojamientoServicioService.getServiciosDisponibles().subscribe({
      next: (servicios) => {
        this.serviciosDisponibles = servicios;
        this.cargandoServicios    = false;
      },
      error: () => {
        this.cargandoServicios = false;
        this.errorServicios    = 'No se pudieron cargar los servicios. Puedes continuar sin seleccionarlos.';
      }
    });
  }

  // ── ALOJ-10: Toggle checkbox — FIX change detection ──────────
  // Se crea un nuevo Set en cada toggle para que Angular detecte
  // el cambio de referencia y re-renderice solo el ítem correcto.

  toggleServicio(servicioId: number): void {
    const nuevos = new Set(this.serviciosSeleccionados);
    if (nuevos.has(servicioId)) {
      nuevos.delete(servicioId);
    } else {
      nuevos.add(servicioId);
    }
    this.serviciosSeleccionados = nuevos;
  }

  isSeleccionado(servicioId: number): boolean {
    return this.serviciosSeleccionados.has(servicioId);
  }

  // ── Getters para acceso fácil desde el template ───────────────
  get name()          { return this.form.get('name'); }
  get description()   { return this.form.get('description'); }
  get address()       { return this.form.get('address'); }
  get city()          { return this.form.get('city'); }
  get pricePerNight() { return this.form.get('pricePerNight'); }
  get maxCapacity()   { return this.form.get('maxCapacity'); }
  get latitude()      { return this.form.get('latitude'); }
  get longitude()     { return this.form.get('longitude'); }
  get mainImage()     { return this.form.get('mainImage'); }

  // ── Clases de estilo por estado del campo ─────────────────────
  campoClase(control: AbstractControl | null): Record<string, boolean> {
    return {
      'border-red-400 bg-red-50':     !!control?.invalid && !!control?.touched,
      'border-green-400 bg-green-50': !!control?.valid   && !!control?.touched,
      'border-neutral-300':           !control?.touched
    };
  }

  // ── Envío del formulario ──────────────────────────────────────

  crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Por favor corrige los errores antes de continuar.';
      return;
    }

    this.isSubmitting   = true;
    this.errorMessage   = '';
    this.successMessage = '';

    const usuario = this.authService.getUsuario();
    const payload = {
      ...this.form.value,
      hostId: usuario?.id ?? 0,
      active: true
    };

    this.alojamientoService.create(payload).subscribe({
      next: (alojamientoCreado) => {
        const serviciosIds = Array.from(this.serviciosSeleccionados);

        if (serviciosIds.length === 0 || !alojamientoCreado.id) {
          this.finalizarCreacion();
          return;
        }

        const peticiones = serviciosIds.map(serviceId =>
          this.alojamientoServicioService
            .addServicio(alojamientoCreado.id!, serviceId)
            .pipe(catchError(() => of(null)))
        );

        forkJoin(peticiones).subscribe({
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