import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';  

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// Átomos
import { ButtonComponent } from './components/ad/atoms/button/button';
import { LabelComponent } from './components/ad/atoms/label/label';
import { InputComponent } from './components/ad/atoms/input/input';
import { IconComponent } from './components/ad/atoms/icon/icon';

import { provideBrowserGlobalErrorListeners } from '@angular/core';

// Moléculas
import { InputFieldComponent } from './components/ad/molecules/input-field/input-field';
import { PasswordFieldComponent } from './components/ad/molecules/password-field/password-field';
import { LoginFormComponent } from './components/ad/molecules/login-form/login-form';

// Organismos
import { LoginCardComponent } from './components/ad/organisms/login-card/login-card';

// Templates
import { LoginTemplateComponent } from './components/ad/templates/login-template/login-template';

// Páginas
import { LoginPageComponent } from './components/ad/pages/login/login';

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
    LoginCardComponent,
    LoginTemplateComponent,
    LoginPageComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    RouterModule.forRoot([])
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient()     
  ],
  bootstrap: [App]
})
export class AppModule { }