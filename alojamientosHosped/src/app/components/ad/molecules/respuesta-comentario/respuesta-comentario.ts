import {
  Component,
  Input,
  OnInit,
  OnDestroy
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import { ComentarioService }    from '../../../../../services/ComentarioService';
import { AuthService }          from '../../../../../services/AuthService';
import {
  RespuestaComentario,
  CrearRespuestaRequest
} from '../../../../models/comentario.model';

/**
 * RespuestaComentarioComponent — Molécula (COMENT-6)
 *
 * Muestra la respuesta del anfitrión debajo de un comentario y,
 * si el usuario autenticado es el anfitrión dueño del alojamiento,
 * presenta un formulario para escribir la respuesta.
 *
 * Flujo:
 *   1. Al iniciar, intenta cargar la respuesta existente del comentario.
 *   2. Si ya existe → muestra el texto de la respuesta (readonly).
 *   3. Si no existe Y el usuario es anfitrión dueño → muestra el formulario.
 *   4. Tras publicar → oculta el formulario y muestra el texto de la respuesta.
 *
 * Reglas de negocio (RF26 / HU-026):
 *   - Solo 1 respuesta por comentario (validado también en backend).
 *   - Solo el anfitrión dueño del alojamiento puede responder.
 *   - Máximo 500 caracteres.
 *
 * Inputs:
 *   @Input() comentarioId   — ID del comentario al que pertenece esta respuesta.
 *   @Input() hostId         — ID del anfitrión dueño del alojamiento.
 *
 * Uso en comentario-card.html:
 *   <app-respuesta-comentario
 *     [comentarioId]="comentario.reservationId"
 *     [hostId]="hostId"
 *   ></app-respuesta-comentario>
 */
@Component({
  selector:    'app-respuesta-comentario',
  standalone:  false,
  templateUrl: './respuesta-comentario.html',
  styleUrls:   ['./respuesta-comentario.scss']
})
export class RespuestaComentarioComponent implements OnInit, OnDestroy {

  // ── Inputs ──────────────────────────────────────────────────────────────────

  /** ID del comentario al que pertenece esta sección de respuesta. */
  @Input() comentarioId!: number;

  /** ID del anfitrión dueño del alojamiento. */
  @Input() hostId!: number;

  // ── Estado ──────────────────────────────────────────────────────────────────

  /** Respuesta existente cargada del backend. null si aún no hay ninguna. */
  respuesta: RespuestaComentario | null = null;

  /** true mientras se consulta el backend al iniciar. */
  cargando = true;

  /** true mientras se envía el formulario. */
  enviando = false;

  /** Mensaje de error del backend. */
  errorEnvio = '';

  /** true si el formulario fue enviado exitosamente (oculta el form). */
  publicado = false;

  /** Formulario reactivo con el campo de texto. */
  form!: FormGroup;

  readonly MAX_TEXT = 500;
  readonly MIN_TEXT = 10;

  private destroy$ = new Subject<void>();

  constructor(
    private fb:                FormBuilder,
    private comentarioService: ComentarioService,
    private authService:       AuthService
  ) {}

  // ── Ciclo de vida ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.form = this.fb.group({
      text: ['', [
        Validators.required,
        Validators.minLength(this.MIN_TEXT),
        Validators.maxLength(this.MAX_TEXT)
      ]]
    });

    this.cargarRespuesta();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Getters ──────────────────────────────────────────────────────────────────

  /**
   * true si el usuario autenticado es el anfitrión dueño de este alojamiento.
   * Determina si se muestra el formulario.
   */
  get esAnfitrionDueno(): boolean {
    const usuario = this.authService.getUsuario();
    return !!usuario?.id && usuario.id === this.hostId;
  }

  /** Caracteres escritos en el textarea. */
  get charCount(): number {
    return (this.form.get('text')?.value ?? '').length;
  }

  /** true si el campo tiene error y fue tocado. */
  get textoInvalido(): boolean {
    const ctrl = this.form.get('text');
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  /** Mensaje de error específico para el textarea. */
  get textoError(): string {
    const ctrl = this.form.get('text');
    if (ctrl?.errors?.['required'])   return 'La respuesta no puede estar vacía.';
    if (ctrl?.errors?.['minlength'])  return `Mínimo ${this.MIN_TEXT} caracteres.`;
    if (ctrl?.errors?.['maxlength'])  return `Máximo ${this.MAX_TEXT} caracteres.`;
    return '';
  }

  /** true si debe mostrarse el formulario para responder. */
  get mostrarFormulario(): boolean {
    return this.esAnfitrionDueno && !this.respuesta && !this.publicado && !this.cargando;
  }

  // ── Lógica ──────────────────────────────────────────────────────────────────

  /**
   * Carga la respuesta existente del comentario.
   * Si el backend responde 404 (sin respuesta), simplemente se deja respuesta = null.
   */
  private cargarRespuesta(): void {
    this.cargando = true;

    this.comentarioService
      .getRespuestasByComentario(this.comentarioId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (lista) => {
          this.respuesta = lista.length > 0 ? lista[0] : null;
          this.cargando  = false;
        },
        error: () => {
          // Sin respuesta o error → no bloquear la UI
          this.respuesta = null;
          this.cargando  = false;
        }
      });
  }

  /** Envía la respuesta al backend. */
  publicar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.enviando) return;

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      this.errorEnvio = 'No se pudo identificar al usuario. Vuelve a iniciar sesión.';
      return;
    }

    const payload: CrearRespuestaRequest = {
      commentId: this.comentarioId,
      hostId:    usuario.id,
      text:      this.form.value.text.trim()
    };

    this.enviando   = true;
    this.errorEnvio = '';

    this.comentarioService
      .respond(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (respuesta) => {
          this.enviando  = false;
          this.publicado = true;
          this.respuesta = respuesta;
        },
        error: (err: Error) => {
          this.enviando   = false;
          this.errorEnvio = err.message || 'Ocurrió un error al publicar la respuesta.';
        }
      });
  }
}
