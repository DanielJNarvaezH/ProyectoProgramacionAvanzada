import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Button } from './app/components/ad/atoms/button/button';
import { Label } from './components/ad/atoms/label/label';
import { Input } from './components/ad/atoms/input/input';

@NgModule({
  declarations: [
    App,
    Button,
    Label,
    Input
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
