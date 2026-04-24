import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import { ComentarioService }          from '../../../../../services/ComentarioService';
import { AuthService }                from '../../../../../services/AuthService';
import { Reserva }                    from '../../../../models/reserva.model';
import { Comentario, CrearComentarioRequest } from '../../../../models/comentario.model';

/**
 * ComentarioFormComponent — Molécula (COMENT-4)
 *
 * Formulario de comentario post-reserva.
 * Permite a un huésped calificar (1-5 estrellas) y escribir un texto
 * sobre un alojamiento, ÚNICAMENTE si su reserva tiene estado COMPLETADA.
 *
 * Reglas de negocio:
 *   - Solo visible / habilitado cuando reserva.status === 'COMPLETADA'
 *   - Calificación: 1-5 (obligatoria), usa StarRatingComponent (COMENT-3)
 *   - Texto: 10-500 caracteres (obligatorio)
 *   - Un solo envío: tras publicar, el formulario se oculta y emite el nuevo comentario
 *
 * Inputs:
 *   @Input() reserva         — Reserva del usuario (debe ser COMPLETADA)
 *   @Input() alojamientoId   — ID del alojamiento (para recargar lista tras publicar)
 *
 * Outputs:
 *   @Output() comentarioPublicado — emite el Comentario creado al componente padre
 *
 * Uso en alojamiento-detalle.html:
 *   <app-comentario-form
 *     [reserva]="reservaCompletada"
 *     [alojamientoId]="alojamiento.id"
 *     (comentarioPublicado)="onNuevoComentario($event)"
 *   ></app-comentario-form>
 */
@Component({
  selector:    'app-comentario-form',
  standalone:  false,
  templateUrl: './comentario-form.html',
  styleUrls:   ['./comentario-form.scss']
})
export class ComentarioFormComponent implements OnInit, OnDestroy {

  // ── Inputs ──────────────────────────────────────────────────────────────────

  /** Reserva del usuario autenticado para este alojamiento. */
  @Input() reserva!: Reserva;

  /** ID del alojamiento al que pertenece la reserva. */
  @Input() alojamientoId!: number;

  // ── Outputs ─────────────────────────────────────────────────────────────────

  /** Emite el comentario recién creado para que el padre actualice la lista. */
  @Output() comentarioPublicado = new EventEmitter<Comentario>();

  // ── Estado del formulario ────────────────────────────────────────────────────

  form!: FormGroup;

  /** true mientras se espera respuesta del backend */
  enviando = false;

  /** Mensaje de error devuelto por el backend */
  errorEnvio = '';

  /** true tras publicar exitosamente → oculta el form y muestra confirmación */
  publicado = false;

  // ── Constantes de validación (COMENT-9) ─────────────────────────────────────

  readonly MIN_TEXT  = 10;
  readonly MAX_TEXT  = 500;

  private destroy$ = new Subject<void>();

  constructor(
    private fb:               FormBuilder,
    private comentarioService: ComentarioService,
    private authService:       AuthService
  ) {}

  // ── Ciclo de vida ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.form = this.fb.group({
      rating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      text:   ['', [
        Validators.required,
        Validators.minLength(this.MIN_TEXT),
        Validators.maxLength(this.MAX_TEXT)
      ]]
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Getters de conveniencia ──────────────────────────────────────────────────

  /** true si la reserva está COMPLETADA → único estado que permite comentar */
  get puedecomentar(): boolean {
    return this.reserva?.status === 'COMPLETADA';
  }

  /** Caracteres escritos en el textarea */
  get charCount(): number {
    return (this.form.get('text')?.value ?? '').length;
  }

  /** true si el campo de texto tiene algún error y fue tocado */
  get textoInvalido(): boolean {
    const ctrl = this.form.get('text');
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  /** Mensaje de error específico para el textarea */
  get textoError(): string {
    const ctrl = this.form.get('text');
    if (ctrl?.errors?.['required'])   return 'El comentario no puede estar vacío.';
    if (ctrl?.errors?.['minlength'])   return `Mínimo ${this.MIN_TEXT} caracteres.`;
    if (ctrl?.errors?.['maxlength'])   return `Máximo ${this.MAX_TEXT} caracteres.`;
    return '';
  }

  /** true si el rating no ha sido seleccionado y el form fue submitted */
  get ratingInvalido(): boolean {
    const ctrl = this.form.get('rating');
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  // ── Acciones ─────────────────────────────────────────────────────────────────

  /** Recibe cambios del StarRatingComponent y actualiza el control del form */
  onRatingChange(valor: number): void {
    this.form.get('rating')?.setValue(valor);
    this.form.get('rating')?.markAsTouched();
  }

  /** Envía el comentario al backend */
  publicar(): void {
    // Marcar todos los campos como tocados para mostrar errores
    this.form.markAllAsTouched();

    if (this.form.invalid || this.enviando) return;

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      this.errorEnvio = 'No se pudo identificar al usuario. Por favor, vuelve a iniciar sesión.';
      return;
    }

    if (!this.reserva?.id) {
      this.errorEnvio = 'No se encontró la reserva asociada.';
      return;
    }

    const payload: CrearComentarioRequest = {
      reservationId: this.reserva.id,
      userId:        usuario.id,
      rating:        this.form.value.rating,
      text:          this.form.value.text.trim()
    };

    this.enviando   = true;
    this.errorEnvio = '';

    this.comentarioService.create(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (comentario) => {
          this.enviando  = false;
          this.publicado = true;
          this.comentarioPublicado.emit(comentario);
        },
        error: (err: Error) => {
          this.enviando   = false;
          this.errorEnvio = err.message || 'Ocurrió un error al publicar el comentario.';
        }
      });
  }

  /** Resetea el formulario para escribir otro comentario (flujo de edición futuro) */
  resetear(): void {
    this.form.reset({ rating: 0, text: '' });
    this.publicado  = false;
    this.errorEnvio = '';
  }
}