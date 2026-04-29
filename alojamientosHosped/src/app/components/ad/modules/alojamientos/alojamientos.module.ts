import { NgModule }                          from '@angular/core';
import { CommonModule }                      from '@angular/common';
import { RouterModule }                      from '@angular/router';
import { ReactiveFormsModule, FormsModule }  from '@angular/forms';

// -- Guards
import { authGuard }      from '../../../../guards/auth.guard';
import { anfitrionGuard } from '../../../../guards/anfitrion.guard';

// -- SharedModule (aporta: NavbarComponent, AlojamientoCardComponent,
//    IconComponent, StarRatingComponent, PesoPipe, SafeUrlPipe,
//    LazyImgDirective, CancelarReservaModalComponent, NotificacionesPanelComponent)
import { SharedModule } from '../shared/shared.module';

// -- Moléculas exclusivas de alojamientos
import { AlojamientoPreviewComponent }       from '../../molecules/alojamiento-preview/alojamiento-preview';
import { ComentarioCardComponent }           from '../../molecules/comentario-card/comentario-card';
import { ComentarioFormComponent }           from '../../molecules/comentario-form/comentario-form';
import { RespuestaComentarioComponent }      from '../../molecules/respuesta-comentario/respuesta-comentario';
import { CalificacionGraficoComponent }      from '../../molecules/calificacion-grafico/calificacion-grafico';
import { ConfirmModalComponent }             from '../../molecules/confirm-modal/confirm-modal';
import { CalendarioDisponibilidadComponent } from '../../molecules/calendario-disponibilidad/calendario-disponibilidad';
import { ReservaResumenModalComponent }      from '../../molecules/reserva-resumen-modal/reserva-resumen-modal';
import { ImageUploaderComponent }            from '../../molecules/image-uploader/image-uploader';
// CancelarReservaModalComponent NO va aquí → viene de SharedModule

// -- Organismos exclusivos de alojamientos
import { GaleriaAlojamientoComponent } from '../../organisms/galeria/galeria-alojamiento';
import { MapaAlojamientosComponent }   from '../../organisms/mapa-alojamientos/mapa-alojamientos';

// -- Páginas
import { AlojamientosListaPageComponent }  from '../../pages/alojamientos-lista/alojamientos-lista';
import { AlojamientoDetallePageComponent } from '../../pages/alojamiento-detalle/alojamiento-detalle';
import { AlojamientoCrearPageComponent }   from '../../pages/alojamiento-crear/alojamiento-crear';
import { AlojamientoEditarPageComponent }  from '../../pages/alojamiento-editar/alojamiento-editar';
import { PanelGestionPageComponent }       from '../../pages/panel-gestion/panel-gestion';

/**
 * AlojamientosModule — INT-3 (Lazy Loading)
 * CancelarReservaModalComponent viene de SharedModule — no se redeclara aquí.
 */
@NgModule({
  declarations: [
    // Moléculas exclusivas
    AlojamientoPreviewComponent,
    ComentarioCardComponent,
    ComentarioFormComponent,
    RespuestaComentarioComponent,
    CalificacionGraficoComponent,
    ConfirmModalComponent,
    CalendarioDisponibilidadComponent,
    ReservaResumenModalComponent,
    ImageUploaderComponent,
    // CancelarReservaModalComponent NO va aquí → está en SharedModule
    // Organismos exclusivos
    GaleriaAlojamientoComponent,
    MapaAlojamientosComponent,
    // Páginas
    AlojamientosListaPageComponent,
    AlojamientoDetallePageComponent,
    AlojamientoCrearPageComponent,
    AlojamientoEditarPageComponent,
    PanelGestionPageComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild([
      { path: '',           canActivate: [authGuard],      component: AlojamientosListaPageComponent },
      { path: 'crear',      canActivate: [anfitrionGuard], component: AlojamientoCrearPageComponent },
      { path: ':id/editar', canActivate: [anfitrionGuard], component: AlojamientoEditarPageComponent },
      { path: ':id',        canActivate: [authGuard],      component: AlojamientoDetallePageComponent },
    ])
  ]
})
export class AlojamientosModule { }