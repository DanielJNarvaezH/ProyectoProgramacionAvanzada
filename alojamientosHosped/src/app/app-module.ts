import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// ✅ Importaciones correctas de tus átomos
import { ButtonComponent } from './components/ad/atoms/button/button';
import { LabelComponent } from './components/ad/atoms/label/label';
import { InputComponent } from './components/ad/atoms/input/input';
import { IconComponent } from './components/ad/atoms/icon/icon';

// ⚙️ Si en algún momento quieres manejar errores globales, puedes dejar esto:
import { provideBrowserGlobalErrorListeners } from '@angular/core';
import { InputFieldComponent } from './components/ad/molecules/input-field/input-field';
import { PasswordFieldComponent } from './components/ad/molecules/password-field/password-field';
import { LoginFormComponent } from './components/ad/molecules/login-form/login-form';
import { LoginCardComponent } from './components/ad/organisms/login-card/login-card';

@NgModule({
  declarations: [
    App,
    ButtonComponent,
    LabelComponent,
    InputComponent,
    IconComponent,
    InputFieldComponent,
    PasswordFieldComponent,
    LoginFormComponent,
    LoginCardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule, // necesario para ngModel
    RouterModule.forRoot([]) // necesario para <router-outlet>
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
