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

import { provideBrowserGlobalErrorListeners } from '@angular/core';

// -- Interceptores
import { AuthInterceptor } from './interceptors/auth.interceptor';

// -- Atomos
import { ButtonComponent }   from './components/ad/atoms/button/button';
import { LabelComponent }    from './components/ad/atoms/label/label';
import { InputComponent }    from './components/ad/atoms/input/input';
import { IconComponent }     from './components/ad/atoms/icon/icon';

// -- Moleculas
import { InputFieldComponent }       from './components/ad/molecules/input-field/input-field';
import { PasswordFieldComponent }    from './components/ad/molecules/password-field/password-field';
import { LoginFormComponent }        from './components/ad/molecules/login-form/login-form';
import { RegisterFormComponent }     from './components/ad/molecules/register-form/register-form';
import { AlojamientoCardComponent }  from './components/ad/molecules/alojamiento-card/alojamiento-card';
import { ComentarioCardComponent }   from './components/ad/molecules/comentario-card/comentario-card';

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

// -- Pipes
import { SafeUrlPipe } from './pipes/safe-url.pipe';

@NgModule({
  declarations: [
    App,
    // Atomos
    ButtonComponent,
    LabelComponent,
    InputComponent,
    IconComponent,
    // Moleculas
    InputFieldComponent,
    PasswordFieldComponent,
    LoginFormComponent,
    RegisterFormComponent,
    AlojamientoCardComponent,
    ComentarioCardComponent,
    ImageUploaderComponent,
    // Organismos
    LoginCardComponent,
    RegisterCardComponent,
    NavbarComponent,
    GaleriaAlojamientoComponent,
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
    PerfilPageComponent,
    // Pipes
    SafeUrlPipe,
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
