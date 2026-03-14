import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService } from '../../../../../services/AuthService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { ServicioDisponible } from '../../../../models/servicio.model';

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

  cargarServicios(): void {
    this.cargandoServicios = true;
    this.errorServicios    = '';

    this.alojamientoServicioService.getServiciosDisponibles().subscribe({
      next: (servicios) => {
        console.log('Servicios recibidos del backend:', servicios);
        this.serviciosDisponibles = servicios;
        this.cargandoServicios    = false;
      },
      error: () => {
        this.cargandoServicios = false;
        this.errorServicios    = 'No se pudieron cargar los servicios. Puedes continuar sin seleccionarlos.';
      }
    });
  }

  toggleServicio(servicioId: number): void {
    console.log('Toggle servicioId:', servicioId, '| tipo:', typeof servicioId);
    const nuevos = new Set(this.serviciosSeleccionados);
    if (nuevos.has(servicioId)) {
      nuevos.delete(servicioId);
    } else {
      nuevos.add(servicioId);
    }
    this.serviciosSeleccionados = nuevos;
    console.log('Set después del toggle:', Array.from(this.serviciosSeleccionados));
  }

  isSeleccionado(servicioId: number): boolean {
    return this.serviciosSeleccionados.has(servicioId);
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