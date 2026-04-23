import {
  Component,
  Input,
  Output,
  EventEmitter,
  forwardRef
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * StarRatingComponent — Átomo (COMENT-3)
 *
 * Rating component interactivo de 1-5 estrellas con selección visual.
 *
 * Soporta dos modos de uso:
 *   1. Modo standalone (Output puro):
 *      <app-star-rating [rating]="valor" (ratingChange)="onCambio($event)">
 *      </app-star-rating>
 *
 *   2. Modo formulario reactivo (ControlValueAccessor):
 *      <app-star-rating formControlName="rating"></app-star-rating>
 *
 * Inputs:
 *   @Input() rating      — valor actual (1-5); default 0 (sin selección)
 *   @Input() readonly    — deshabilita la interacción (solo visualización)
 *   @Input() size        — 'sm' | 'md' | 'lg'  (default 'md')
 *   @Input() showLabel   — muestra etiqueta de texto junto a las estrellas
 *
 * Outputs:
 *   @Output() ratingChange — emite el nuevo valor al hacer clic
 *
 * Accesibilidad:
 *   - Rol `group` con `aria-label` descriptivo
 *   - Cada estrella es un `button` con `aria-label` y `aria-pressed`
 *   - Navegación por teclado (Enter / Space nativos del button)
 */
@Component({
  selector: 'app-star-rating',
  standalone: false,
  templateUrl: './star-rating.html',
  styleUrls: ['./star-rating.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => StarRatingComponent),
      multi: true
    }
  ]
})
export class StarRatingComponent implements ControlValueAccessor {

  // ── Inputs ────────────────────────────────────────────────────────────────

  /** Valor de calificación actual (1-5). 0 = sin selección. */
  @Input() rating = 0;

  /** Cuando es true, las estrellas se muestran pero no se pueden cambiar. */
  @Input() readonly = false;

  /** Tamaño visual del componente: 'sm' | 'md' | 'lg'. */
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  /** Muestra el label de texto (p. ej. "4 de 5 estrellas") al lado. */
  @Input() showLabel = false;

  // ── Outputs ───────────────────────────────────────────────────────────────

  /** Emite el nuevo valor de rating cuando el usuario hace clic en una estrella. */
  @Output() ratingChange = new EventEmitter<number>();

  // ── Estado interno ────────────────────────────────────────────────────────

  /** Índice de la estrella sobre la que está el cursor (hover), -1 si ninguna. */
  hoverIndex = -1;

  /** Array fijo [1, 2, 3, 4, 5] para iterar en el template. */
  readonly stars = [1, 2, 3, 4, 5];

  /** Labels descriptivos para accesibilidad. */
  readonly starLabels = [
    'Muy malo',
    'Malo',
    'Regular',
    'Bueno',
    'Excelente'
  ];

  // ── ControlValueAccessor ──────────────────────────────────────────────────

  private onChange: (value: number) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: number): void {
    this.rating = value ?? 0;
  }

  registerOnChange(fn: (value: number) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.readonly = isDisabled;
  }

  // ── Lógica de interacción ─────────────────────────────────────────────────

  /**
   * Determina si una estrella (índice 1-5) debe mostrarse como "llena".
   * Prioriza el estado hover sobre el rating seleccionado.
   */
  isActive(star: number): boolean {
    const efectivo = this.hoverIndex >= 0 ? this.hoverIndex : this.rating;
    return star <= efectivo;
  }

  /** Cambia el rating al hacer clic en una estrella. */
  select(star: number): void {
    if (this.readonly) return;
    this.rating = star;
    this.ratingChange.emit(star);
    this.onChange(star);
    this.onTouched();
  }

  /** Activa el efecto hover al entrar el cursor en una estrella. */
  onMouseEnter(star: number): void {
    if (this.readonly) return;
    this.hoverIndex = star;
  }

  /** Desactiva el efecto hover al salir el cursor del grupo. */
  onMouseLeave(): void {
    this.hoverIndex = -1;
  }

  /** Texto descriptivo del rating activo (para el label y aria). */
  get ratingLabel(): string {
    const val = this.hoverIndex >= 0 ? this.hoverIndex : this.rating;
    if (val === 0) return 'Sin calificación';
    return `${val} de 5 — ${this.starLabels[val - 1]}`;
  }
}
