import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { AlojamientosListaPageComponent } from './components/ad/pages/alojamientos-lista/alojamientos-lista';
import { ImageUploaderComponent } from './components/ad/molecules/image-uploader/image-uploader';
import { AlojamientoPreviewComponent } from './components/ad/molecules/alojamiento-preview/alojamiento-preview';
import { MapaAlojamientosComponent } from './components/ad/organisms/mapa-alojamientos/mapa-alojamientos';

import { provideBrowserGlobalErrorListeners } from '@angular/core';

// -- Interceptores
import { AuthInterceptor } from './interceptors/auth.interceptor';

// -- Atomos
import { ButtonComponent }    from './components/ad/atoms/button/button';
import { LabelComponent }     from './components/ad/atoms/label/label';
import { InputComponent }     from './components/ad/atoms/input/input';
import { IconComponent }      from './components/ad/atoms/icon/icon';
import { StarRatingComponent } from './components/ad/atoms/star-rating/star-rating'; // COMENT-3

// -- Moleculas
import { InputFieldComponent }            from './components/ad/molecules/input-field/input-field';
import { PasswordFieldComponent }         from './components/ad/molecules/password-field/password-field';
import { LoginFormComponent }             from './components/ad/molecules/login-form/login-form';
import { RegisterFormComponent }          from './components/ad/molecules/register-form/register-form';
import { AlojamientoCardComponent }       from './components/ad/molecules/alojamiento-card/alojamiento-card';
import { ComentarioCardComponent }        from './components/ad/molecules/comentario-card/comentario-card';
import { ComentarioFormComponent }        from './components/ad/molecules/comentario-form/comentario-form';         // COMENT-4
import { RespuestaComentarioComponent }   from './components/ad/molecules/respuesta-comentario/respuesta-comentario'; // COMENT-6
import { CalificacionGraficoComponent }   from './components/ad/molecules/calificacion-grafico/calificacion-grafico'; // COMENT-10
import { ConfirmModalComponent }           from './components/ad/molecules/confirm-modal/confirm-modal';
import { CalendarioDisponibilidadComponent } from './components/ad/molecules/calendario-disponibilidad/calendario-disponibilidad';
import { ReservaResumenModalComponent }   from './components/ad/molecules/reserva-resumen-modal/reserva-resumen-modal'; // RESERV-7
import { CancelarReservaModalComponent }  from './components/ad/molecules/cancelar-reserva-modal/cancelar-reserva-modal'; // RESERV-10
import { NotificacionesPanelComponent }   from './components/ad/molecules/notificaciones-panel/notificaciones-panel';

// -- Organismos
import { LoginCardComponent }             from './components/ad/organisms/login-card/login-card';
import { RegisterCardComponent }          from './components/ad/organisms/register-card/register-card';
import { NavbarComponent }                from './components/ad/organisms/navbar/navbar';
import { GaleriaAlojamientoComponent }    from './components/ad/organisms/galeria/galeria-alojamiento';

// -- Templates
import { LoginTemplateComponent }    from './components/ad/templates/login-template/login-template';
import { RegisterTemplateComponent } from './components/ad/templates/register-template/register-template';

// -- Paginas
import { LoginPageComponent }              from './components/ad/pages/login/login';
import { RegisterPageComponent }           from './components/ad/pages/register/register';
import { RecuperarContrasenaComponent }    from './components/ad/pages/recuperar-contrasena/recuperar-contrasena';
import { PerfilPageComponent }             from './components/ad/pages/perfil/perfil';
import { AlojamientoEditarPageComponent }  from './components/ad/pages/alojamiento-editar/alojamiento-editar';
import { AlojamientoDetallePageComponent } from './components/ad/pages/alojamiento-detalle/alojamiento-detalle';
import { AlojamientoCrearPageComponent }   from './components/ad/pages/alojamiento-crear/alojamiento-crear';
import { PanelGestionPageComponent }       from './components/ad/pages/panel-gestion/panel-gestion'; // ALOJ-9
import { MisFavoritosPageComponent }       from './components/ad/pages/mis-favoritos/mis-favoritos';  // ALOJ-21
import { MisReservasPageComponent }        from './components/ad/pages/mis-reservas/mis-reservas';    // RESERV-8

// -- Pipes
import { SafeUrlPipe }    from './pipes/safe-url.pipe';
import { PesoPipe }       from './pipes/peso.pipe';

// -- Directivas
import { LazyImgDirective } from './directives/lazy-img.directive';

@NgModule({
  declarations: [
    App,
    // Atomos
    ButtonComponent,
    LabelComponent,
    InputComponent,
    IconComponent,
    StarRatingComponent,              // COMENT-3
    // Moleculas
    InputFieldComponent,
    PasswordFieldComponent,
    LoginFormComponent,
    RegisterFormComponent,
    AlojamientoCardComponent,
    ComentarioCardComponent,
    ComentarioFormComponent,          // COMENT-4
    RespuestaComentarioComponent,      // COMENT-6
    CalificacionGraficoComponent,     // COMENT-10
    ImageUploaderComponent,
    AlojamientoPreviewComponent,
    ConfirmModalComponent,
    CalendarioDisponibilidadComponent,
    ReservaResumenModalComponent,     // RESERV-7
    CancelarReservaModalComponent,    // RESERV-10
    NotificacionesPanelComponent,

    // Organismos
    LoginCardComponent,
    RegisterCardComponent,
    NavbarComponent,
    GaleriaAlojamientoComponent,
    MapaAlojamientosComponent,

    // Templates
    LoginTemplateComponent,
    RegisterTemplateComponent,
    // Paginas
    LoginPageComponent,
    RegisterPageComponent,
    AlojamientosListaPageComponent,
    AlojamientoDetallePageComponent,
    AlojamientoCrearPageComponent,
    AlojamientoEditarPageComponent,  // ALOJ-8
    PanelGestionPageComponent,       // ALOJ-9
    MisFavoritosPageComponent,       // ALOJ-21
    MisReservasPageComponent,        // RESERV-8
    PerfilPageComponent,
    // Pipes
    SafeUrlPipe,
    PesoPipe,
    // Directivas
    LazyImgDirective,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot([]),
    RecuperarContrasenaComponent
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide:  HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi:    true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
