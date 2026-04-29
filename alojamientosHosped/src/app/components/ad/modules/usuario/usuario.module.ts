import { NgModule }            from '@angular/core';
import { CommonModule }        from '@angular/common';
import { RouterModule }        from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// -- Guards
import { authGuard } from '../../../../guards/auth.guard';

// -- SharedModule (aporta: NavbarComponent, AlojamientoCardComponent,
//    IconComponent, StarRatingComponent, PesoPipe, LazyImgDirective,
//    CancelarReservaModalComponent, NotificacionesPanelComponent)
import { SharedModule } from '../shared/shared.module';

// -- Páginas
import { PerfilPageComponent }       from '../../pages/perfil/perfil';
import { MisFavoritosPageComponent } from '../../pages/mis-favoritos/mis-favoritos';
import { MisReservasPageComponent }  from '../../pages/mis-reservas/mis-reservas';

/**
 * UsuarioModule — INT-3 (Lazy Loading)
 * CancelarReservaModalComponent viene de SharedModule — no se redeclara aquí.
 * No hay declaraciones exclusivas de moléculas — todo viene de SharedModule.
 */
@NgModule({
  declarations: [
    // CancelarReservaModalComponent NO va aquí → está en SharedModule
    // Páginas
    PerfilPageComponent,
    MisFavoritosPageComponent,
    MisReservasPageComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild([
      { path: 'perfil',        canActivate: [authGuard], component: PerfilPageComponent },
      { path: 'mis-favoritos', canActivate: [authGuard], component: MisFavoritosPageComponent },
      { path: 'mis-reservas',  canActivate: [authGuard], component: MisReservasPageComponent },
    ])
  ]
})
export class UsuarioModule { }