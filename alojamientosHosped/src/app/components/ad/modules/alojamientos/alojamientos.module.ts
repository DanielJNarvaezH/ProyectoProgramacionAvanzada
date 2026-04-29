import { NgModule }                          from '@angular/core';
import { CommonModule }                      from '@angular/common';
import { RouterModule }                      from '@angular/router';
import { ReactiveFormsModule, FormsModule }  from '@angular/forms';

// -- Guards
import { authGuard }      from '../../../../guards/auth.guard';
import { anfitrionGuard } from '../../../../guards/anfitrion.guard';

// -- SharedModule (aporta: ConfirmModalComponent, NavbarComponent, PesoPipe, SafeUrlPipe, LazyImgDirective, etc.)
import { SharedModule } from '../shared/shared.module';

// -- Moléculas exclusivas de alojamientos
import { AlojamientoPreviewComponent }       from '../../molecules/alojamiento-preview/alojamiento-preview';
import { ComentarioCardComponent }           from '../../molecules/comentario-card/comentario-card';
import { ComentarioFormComponent }           from '../../molecules/comentario-form/comentario-form';
import { RespuestaComentarioComponent }      from '../../molecules/respuesta-comentario/respuesta-comentario';
import { CalificacionGraficoComponent }      from '../../molecules/calificacion-grafico/calificacion-grafico';
import { CalendarioDisponibilidadComponent } from '../../molecules/calendario-disponibilidad/calendario-disponibilidad';
import { ReservaResumenModalComponent }      from '../../molecules/reserva-resumen-modal/reserva-resumen-modal';
import { ImageUploaderComponent }            from '../../molecules/image-uploader/image-uploader';
// ConfirmModalComponent → viene de SharedModule

// -- Organismos exclusivos de alojamientos
import { GaleriaAlojamientoComponent } from '../../organisms/galeria/galeria-alojamiento';
import { MapaAlojamientosComponent }   from '../../organisms/mapa-alojamientos/mapa-alojamientos';

// -- Páginas (sin PanelGestionPageComponent → se movió a PanelModule)
import { AlojamientosListaPageComponent }  from '../../pages/alojamientos-lista/alojamientos-lista';
import { AlojamientoDetallePageComponent } from '../../pages/alojamiento-detalle/alojamiento-detalle';
import { AlojamientoCrearPageComponent }   from '../../pages/alojamiento-crear/alojamiento-crear';
import { AlojamientoEditarPageComponent }  from '../../pages/alojamiento-editar/alojamiento-editar';

@NgModule({
  declarations: [
    AlojamientoPreviewComponent,        // usa appLazyImg, safeUrl, peso → vienen de SharedModule
    ComentarioCardComponent,
    ComentarioFormComponent,
    RespuestaComentarioComponent,
    CalificacionGraficoComponent,
    // ConfirmModalComponent → viene de SharedModule
    CalendarioDisponibilidadComponent,
    ReservaResumenModalComponent,
    ImageUploaderComponent,
    GaleriaAlojamientoComponent,
    MapaAlojamientosComponent,
    AlojamientosListaPageComponent,
    AlojamientoDetallePageComponent,
    AlojamientoCrearPageComponent,
    AlojamientoEditarPageComponent,
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