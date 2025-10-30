import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// 🔹 Si aún no has creado estos componentes, comenta o elimina estas líneas temporalmente
// import { Button } from './components/ad/atoms/button/button';
// import { Label } from './components/ad/atoms/label/label';
// import { Input } from './components/ad/atoms/input/input';

@NgModule({
  declarations: [
    App,
    // Button,
    // Label,
    // Input
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule.forRoot([]) // Necesario para que funcione <router-outlet>
  ],
  providers: [],
  bootstrap: [App]
})
export class AppModule { }
