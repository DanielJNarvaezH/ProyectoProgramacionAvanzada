import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { Alojamiento }        from '../../../../models/alojamiento.model';

/**
 * AlojamientoEditarPageComponent — ALOJ-8
 *
 * Formulario de edición de alojamiento existente.
 * - Carga los datos actuales del alojamiento por ID
 * - Permite modificar todos los campos editables
 * - Llama a AlojamientoService.update() al guardar
 * - Ruta: /alojamientos/:id/editar
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

  cargando     = true;
  guardando    = false;
  errorCarga   = '';
  errorGuardar = '';
  exito        = false;

  private destroy$ = new Subject<void>();

  constructor(
    private fb:                 FormBuilder,
    private route:              ActivatedRoute,
    private router:             Router,
    private alojamientoService: AlojamientoService
  ) {}

  ngOnInit(): void {
    this.alojamientoId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.alojamientoId) {
      this.router.navigate(['/alojamientos']);
      return;
    }
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
      latitude:      [null, [Validators.required, Validators.min(-90), Validators.max(90)]],
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

    this.alojamientoService.getById(this.alojamientoId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (aloj) => {
          this.form.patchValue({
            name:          aloj.name,
            description:   aloj.description,
            address:       aloj.address,
            city:          aloj.city,
            latitude:      aloj.latitude,
            longitude:     aloj.longitude,
            pricePerNight: aloj.pricePerNight,
            maxCapacity:   aloj.maxCapacity,
            mainImage:     aloj.mainImage ?? '',
            active:        aloj.active ?? true
          });
          this.cargando = false;
        },
        error: (err) => {
          this.errorCarga = err.message || 'No se pudo cargar el alojamiento.';
          this.cargando   = false;
        }
      });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando    = true;
    this.errorGuardar = '';
    this.exito        = false;

    const payload: Alojamiento = {
      ...this.form.value,
      hostId: 0
    };

    this.alojamientoService.update(this.alojamientoId, payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.guardando = false;
          this.exito     = true;
          setTimeout(() => this.router.navigate(['/alojamientos', this.alojamientoId]), 1800);
        },
        error: (err) => {
          this.guardando    = false;
          this.errorGuardar = err.message || 'Error al guardar los cambios. Intenta de nuevo.';
        }
      });
  }

  cancelar(): void {
    this.router.navigate(['/alojamientos', this.alojamientoId]);
  }

  campo(name: string) {
    return this.form.get(name);
  }

  invalido(name: string): boolean {
    const c = this.campo(name);
    return !!(c && c.invalid && c.touched);
  }

  errorMsg(name: string): string {
    const c = this.campo(name);
    if (!c || !c.errors) return '';
    if (c.errors['required'])  return 'Este campo es obligatorio.';
    if (c.errors['minlength']) return `Mínimo ${c.errors['minlength'].requiredLength} caracteres.`;
    if (c.errors['maxlength']) return `Máximo ${c.errors['maxlength'].requiredLength} caracteres.`;
    if (c.errors['min'])       return `El valor mínimo es ${c.errors['min'].min}.`;
    if (c.errors['max'])       return `El valor máximo es ${c.errors['max'].max}.`;
    return 'Valor inválido.';
  }
}
