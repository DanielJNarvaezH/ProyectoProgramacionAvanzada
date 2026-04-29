import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/**
 * ToastService — INT-1
 *
 * Sistema unificado de notificaciones toast para toda la app.
 * Uso desde cualquier componente:
 *
 *   this.toastService.success('Reserva creada correctamente');
 *   this.toastService.error('No se pudo procesar la solicitud');
 *   this.toastService.info('Tienes 3 notificaciones nuevas');
 *   this.toastService.warning('Recuerda completar tu perfil');
 */

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id:       number;
  type:     ToastType;
  message:  string;
  duration: number;  // ms
}

@Injectable({ providedIn: 'root' })
export class ToastService {

  private counter = 0;
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  // ── Métodos públicos ──────────────────────────────────────────

  success(message: string, duration = 3500): void {
    this.add('success', message, duration);
  }

  error(message: string, duration = 5000): void {
    this.add('error', message, duration);
  }

  info(message: string, duration = 3500): void {
    this.add('info', message, duration);
  }

  warning(message: string, duration = 4000): void {
    this.add('warning', message, duration);
  }

  dismiss(id: number): void {
    this.toastsSubject.next(
      this.toastsSubject.value.filter(t => t.id !== id)
    );
  }

  // ── Interno ───────────────────────────────────────────────────

  private add(type: ToastType, message: string, duration: number): void {
    const id    = ++this.counter;
    const toast: Toast = { id, type, message, duration };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    setTimeout(() => this.dismiss(id), duration);
  }
}
