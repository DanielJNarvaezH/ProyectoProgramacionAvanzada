import { NgModule }     from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// -- Atomos compartidos
import { IconComponent }       from '../../atoms/icon/icon';
import { StarRatingComponent } from '../../atoms/star-rating/star-rating';

// -- Moléculas compartidas
import { AlojamientoCardComponent }      from '../../molecules/alojamiento-card/alojamiento-card';
import { NotificacionesPanelComponent }  from '../../molecules/notificaciones-panel/notificaciones-panel';
import { CancelarReservaModalComponent } from '../../molecules/cancelar-reserva-modal/cancelar-reserva-modal';

// -- Organismos compartidos
import { NavbarComponent } from '../../organisms/navbar/navbar';

// -- Pipes compartidos
import { PesoPipe }    from '../../../../pipes/peso.pipe';
import { SafeUrlPipe } from '../../../../pipes/safe-url.pipe';

// -- Directivas compartidas
import { LazyImgDirective } from '../../../../directives/lazy-img.directive';

/**
 * SharedModule — INT-3 (Lazy Loading)
 *
 * Declara y exporta todo lo que se usa en más de un feature module.
 * Regla Angular: standalone:false solo puede declararse en UN NgModule.
 * Importado por: AuthModule, AlojamientosModule, UsuarioModule.
 */
@NgModule({
  declarations: [
    IconComponent,
    StarRatingComponent,
    AlojamientoCardComponent,
    NotificacionesPanelComponent,
    CancelarReservaModalComponent,
    NavbarComponent,
    PesoPipe,
    SafeUrlPipe,
    LazyImgDirective,
  ],
  imports: [
    CommonModule,
    RouterModule,
  ],
  exports: [
    CommonModule,
    RouterModule,
    IconComponent,
    StarRatingComponent,
    AlojamientoCardComponent,
    NotificacionesPanelComponent,
    CancelarReservaModalComponent,
    NavbarComponent,
    PesoPipe,
    SafeUrlPipe,
    LazyImgDirective,
  ]
})
export class SharedModule { }