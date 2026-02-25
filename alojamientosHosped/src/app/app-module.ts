import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

import { provideBrowserGlobalErrorListeners } from '@angular/core';

// ── Interceptores ──────────────────────────────────────────────────
import { AuthInterceptor } from './interceptors/auth.interceptor';  // ← NUEVO

// ── Átomos ────────────────────────────────────────────────────────
import { ButtonComponent }   from './components/ad/atoms/button/button';
import { LabelComponent }    from './components/ad/atoms/label/label';
import { InputComponent }    from './components/ad/atoms/input/input';
import { IconComponent }     from './components/ad/atoms/icon/icon';

// ── Moléculas ─────────────────────────────────────────────────────
import { InputFieldComponent }    from './components/ad/molecules/input-field/input-field';
import { PasswordFieldComponent } from './components/ad/molecules/password-field/password-field';
import { LoginFormComponent }     from './components/ad/molecules/login-form/login-form';
import { RegisterFormComponent }  from './components/ad/molecules/register-form/register-form';

// ── Organismos ────────────────────────────────────────────────────
import { LoginCardComponent }    from './components/ad/organisms/login-card/login-card';
import { RegisterCardComponent } from './components/ad/organisms/register-card/register-card';

// ── Templates ─────────────────────────────────────────────────────
import { LoginTemplateComponent }    from './components/ad/templates/login-template/login-template';
import { RegisterTemplateComponent } from './components/ad/templates/register-template/register-template';

// ── Páginas ───────────────────────────────────────────────────────
import { LoginPageComponent }    from './components/ad/pages/login/login';
import { RegisterPageComponent } from './components/ad/pages/register/register';

@NgModule({
  declarations: [
    App,
    // Átomos
    ButtonComponent,
    LabelComponent,
    InputComponent,
    IconComponent,
    // Moléculas
    InputFieldComponent,
    PasswordFieldComponent,
    LoginFormComponent,
    RegisterFormComponent,
    // Organismos
    LoginCardComponent,
    RegisterCardComponent,
    // Templates
    LoginTemplateComponent,
    RegisterTemplateComponent,
    // Páginas
    LoginPageComponent,
    RegisterPageComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot([])
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptorsFromDi()),   // ← cambiado: necesario para interceptores de clase
    {
      provide:  HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi:    true                               // ← permite múltiples interceptores en cadena
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
