import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService } from '../../../../../services/AuthService';

/**
 * AlojamientoCrearPageComponent — ALOJ-12
 *
 * Formulario de creación de alojamiento con validaciones:
 * - Precio por noche > 0
 * - Capacidad máxima >= 1
 * - Latitud entre -90 y 90 / Longitud entre -180 y 180
 * - Imagen principal obligatoria (URL)
 */
@Component({
  selector: 'app-alojamiento-crear',
  standalone: false,
  templateUrl: './alojamiento-crear.html',
  styleUrls: ['./alojamiento-crear.scss']
})
export class AlojamientoCrearPageComponent implements OnInit {

  form!: FormGroup;

  isSubmitting  = false;
  errorMessage  = '';
  successMessage = '';

  constructor(
    private fb:                 FormBuilder,
    private alojamientoService: AlojamientoService,
    private authService:        AuthService,
    private router:             Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:          ['', [Validators.required, Validators.maxLength(150)]],
      description:   ['', [Validators.required]],
      address:       ['', [Validators.required, Validators.maxLength(255)]],
      city:          ['', [Validators.required, Validators.maxLength(100)]],
      // ALOJ-12: precio > 0
      pricePerNight: [null, [Validators.required, Validators.min(0.01)]],
      // ALOJ-12: capacidad >= 1
      maxCapacity:   [null, [Validators.required, Validators.min(1)]],
      // ALOJ-12: coordenadas válidas
      latitude:      [null, [Validators.required, Validators.min(-90),  Validators.max(90)]],
      longitude:     [null, [Validators.required, Validators.min(-180), Validators.max(180)]],
      // ALOJ-12: imagen obligatoria
      mainImage:     ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]]
    });
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

    this.isSubmitting  = true;
    this.errorMessage  = '';
    this.successMessage = '';

    const usuario = this.authService.getUsuario();
    const payload = {
      ...this.form.value,
      hostId: usuario?.id ?? 0,
      active: true
    };

    this.alojamientoService.create(payload).subscribe({
      next: () => {
        this.isSubmitting   = false;
        this.successMessage = '¡Alojamiento creado exitosamente!';
        setTimeout(() => this.router.navigate(['/alojamientos']), 1500);
      },
      error: (err: Error) => {
        this.isSubmitting = false;
        this.errorMessage = err.message || 'Error al crear el alojamiento.';
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/alojamientos']);
  }
}
