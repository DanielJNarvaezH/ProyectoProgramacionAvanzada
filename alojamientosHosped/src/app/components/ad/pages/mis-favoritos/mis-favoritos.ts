import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin, of, takeUntil } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { FavoritoService }    from '../../../../../services/FavoritoService';
import { AlojamientoService } from '../../../../../services/AlojamientoService';
import { AuthService }        from '../../../../../services/AuthService';
import { Alojamiento }        from '../../../../models';

/**
 * MisFavoritosPageComponent — ALOJ-21
 *
 * Página que muestra los alojamientos marcados como favoritos
 * por el usuario autenticado.
 *
 * Flujo:
 *  1. Obtiene los favoritos del usuario via GET /api/favoritos/usuario/:id
 *  2. Por cada favorito carga el alojamiento completo para renderizar la card
 *  3. Permite quitar favoritos directamente desde esta vista
 */
@Component({
  selector: 'app-mis-favoritos',
  standalone: false,
  templateUrl: './mis-favoritos.html',
  styleUrls: ['./mis-favoritos.scss']
})
export class MisFavoritosPageComponent implements OnInit, OnDestroy {

  alojamientos: Alojamiento[] = [];
  cargando   = false;
  error      = '';

  // Map alojamientoId → se está quitando (spinner individual)
  quitando = new Map<number, boolean>();

  private destroy$ = new Subject<void>();

  constructor(
    private favoritoService:    FavoritoService,
    private alojamientoService: AlojamientoService,
    private authService:        AuthService
  ) {}

  ngOnInit(): void {
    this.cargarFavoritos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Carga de datos ────────────────────────────────────────────

  cargarFavoritos(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      this.error = 'No se pudo identificar al usuario.';
      return;
    }

    this.cargando = true;
    this.error    = '';

    this.favoritoService.listarPorUsuario(usuario.id)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(favoritos => {
          if (favoritos.length === 0) return of([] as Alojamiento[]);
          // Cargar el alojamiento completo de cada favorito en paralelo
          const peticiones = favoritos.map(f =>
            this.alojamientoService.getById(f.lodgingId)
              .pipe(catchError(() => of(null)))
          );
          return forkJoin(peticiones);
        })
      )
      .subscribe({
        next: (resultados) => {
          // Filtrar nulos (alojamientos que ya no existen)
          this.alojamientos = (resultados as (Alojamiento | null)[])
            .filter((a): a is Alojamiento => a !== null);
          this.cargando = false;
        },
        error: (err) => {
          this.error    = err.message || 'Error al cargar los favoritos.';
          this.cargando = false;
        }
      });
  }

  // ── Quitar favorito ───────────────────────────────────────────

  quitarFavorito(alojamiento: Alojamiento): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id || !alojamiento.id) return;

    this.quitando.set(alojamiento.id, true);

    this.favoritoService.eliminar(usuario.id, alojamiento.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.alojamientos = this.alojamientos.filter(a => a.id !== alojamiento.id);
          this.quitando.delete(alojamiento.id!);
        },
        error: () => {
          this.quitando.delete(alojamiento.id!);
        }
      });
  }

  // ── Helpers ───────────────────────────────────────────────────

  get idsAlojamientos(): number[] {
    return this.alojamientos.map(a => a.id!).filter(id => !!id);
  }

  estaQuitando(id: number): boolean {
    return this.quitando.get(id) === true;
  }

  trackById(_: number, item: Alojamiento): number | undefined {
    return item.id;
  }
}
