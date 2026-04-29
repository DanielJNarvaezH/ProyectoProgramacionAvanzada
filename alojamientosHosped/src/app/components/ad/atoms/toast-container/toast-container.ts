import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ToastService, Toast } from '../../../../../services/ToastService';
/**
 * ToastContainerComponent — INT-1
 *
 * Contenedor global de toasts. Se coloca una sola vez en app.html.
 * Se suscribe a ToastService.toasts$ y renderiza la pila de mensajes.
 */
@Component({
  selector: 'app-toast-container',
  standalone: false,
  templateUrl: './toast-container.html',
  styleUrls: ['./toast-container.scss']
})
export class ToastContainerComponent implements OnInit, OnDestroy {

  toasts: Toast[] = [];
  private destroy$ = new Subject<void>();

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.toastService.toasts$
      .pipe(takeUntil(this.destroy$))
      .subscribe(toasts => this.toasts = toasts);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success': return 'fa-solid fa-circle-check';
      case 'error':   return 'fa-solid fa-circle-xmark';
      case 'warning': return 'fa-solid fa-triangle-exclamation';
      default:        return 'fa-solid fa-circle-info';
    }
  }

  trackById(_: number, t: Toast): number { return t.id; }
}
