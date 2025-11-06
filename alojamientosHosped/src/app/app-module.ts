import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// ✅ Importaciones correctas
import { ButtonComponent } from './components/ad/atoms/button/button';
import { LabelComponent } from './components/ad/atoms/label/label';
import { InputComponent } from './components/ad/atoms/input/input';
import { IconComponent } from './components/ad/atoms/icon/icon';

@NgModule({
  declarations: [
    App,
    ButtonComponent,
    LabelComponent,
    InputComponent,
    IconComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule // necesario por el ngModel del input
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }

