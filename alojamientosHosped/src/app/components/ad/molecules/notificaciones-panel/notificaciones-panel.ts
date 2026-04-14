import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { NotificacionService } from '../../../../../services/NotificacionService';
import { AuthService }         from '../../../../../services/AuthService';
import {
  Notificacion,
  TIPO_NOTIFICACION_ICON,
  TIPO_NOTIFICACION_COLOR
} from '../../../../models/notificacion.model';

@Component({
  selector: 'app-notificaciones-panel',
  standalone: false,
  templateUrl: './notificaciones-panel.html',
  styleUrls: ['./notificaciones-panel.scss']
})
export class NotificacionesPanelComponent implements OnInit, OnDestroy {

  notificaciones: Notificacion[] = [];
  abierto  = false;
  cargando = false;
  noLeidas = 0;

  readonly ICON  = TIPO_NOTIFICACION_ICON;
  readonly COLOR = TIPO_NOTIFICACION_COLOR;

  private usuarioId: number | null = null;
  private destroy$  = new Subject<void>();
  private pollingInterval: any;

  constructor(
    private notifService: NotificacionService,
    private authService:  AuthService
  ) {}

  ngOnInit(): void {
    const usuario = this.authService.getUsuario();
    this.usuarioId = usuario?.id ?? null;
    if (!this.usuarioId) return;

    this.cargarNotificaciones();

    // Polling cada 60 segundos
    this.pollingInterval = setInterval(() => {
      this.actualizarContador();
    }, 60000);

    this.notifService.noLeidas$
      .pipe(takeUntil(this.destroy$))
      .subscribe(n => this.noLeidas = n);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.pollingInterval) clearInterval(this.pollingInterval);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const el = event.target as HTMLElement;
    if (!el.closest('.notif-wrapper')) {
      this.abierto = false;
    }
  }

  togglePanel(event: Event): void {
    event.stopPropagation();
    this.abierto = !this.abierto;
    if (this.abierto && (this.notificaciones ?? []).length === 0) {
      this.cargarNotificaciones();
    }
  }

  cargarNotificaciones(): void {
    if (!this.usuarioId) return;
    this.cargando = true;

    this.notifService.getPorUsuario(this.usuarioId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(lista => {
        this.notificaciones = lista ?? [];
        this.noLeidas = this.notificaciones.filter(n => !n.read).length;
        this.notifService.actualizarBadge(this.noLeidas);
        this.cargando = false;
      });
  }

  private actualizarContador(): void {
    if (!this.usuarioId) return;
    this.notifService.actualizarContador(this.usuarioId);
  }

  marcarLeida(notif: Notificacion, event: Event): void {
    event.stopPropagation();
    if (notif.read || !notif.id) return;

    this.notifService.marcarLeida(notif.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.notificaciones = this.notificaciones.map(n =>
          n.id === notif.id ? { ...n, read: true } : n
        );
        this.noLeidas = Math.max(0, this.noLeidas - 1);
        this.notifService.actualizarBadge(this.noLeidas);
      });
  }

  marcarTodasLeidas(): void {
    if (!this.usuarioId) return;
    this.notifService.marcarTodasLeidas(this.usuarioId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.notificaciones = this.notificaciones.map(n => ({ ...n, read: true }));
        this.noLeidas = 0;
        this.notifService.actualizarBadge(0);
      });
  }

  eliminar(notif: Notificacion, event: Event): void {
    event.stopPropagation();
    if (!notif.id) return;

    this.notifService.eliminar(notif.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (!notif.read) this.noLeidas = Math.max(0, this.noLeidas - 1);
        this.notificaciones = this.notificaciones.filter(n => n.id !== notif.id);
        this.notifService.actualizarBadge(this.noLeidas);
      });
  }

  formatearFecha(fecha: string | undefined): string {
    if (!fecha) return '';
    const d = new Date(fecha);
    if (isNaN(d.getTime())) return '';
    const ahora  = new Date();
    const diffMs = ahora.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffH   = Math.floor(diffMin / 60);
    const diffD   = Math.floor(diffH / 24);

    if (diffMin < 1)  return 'Ahora';
    if (diffMin < 60) return `Hace ${diffMin} min`;
    if (diffH < 24)   return `Hace ${diffH}h`;
    if (diffD < 7)    return `Hace ${diffD}d`;
    return d.toLocaleDateString('es-CO', { day: 'numeric', month: 'short' });
  }

  getIcon(type: string): string {
    return this.ICON[type] ?? 'fa-solid fa-bell';
  }

  getColor(type: string): string {
    return this.COLOR[type] ?? 'neutral';
  }

  get hayNoLeidas(): boolean { return this.noLeidas > 0; }
  get totalLabel(): string {
    return this.noLeidas > 9 ? '9+' : String(this.noLeidas);
  }

  trackById(_: number, n: Notificacion): number { return n.id ?? 0; }
}