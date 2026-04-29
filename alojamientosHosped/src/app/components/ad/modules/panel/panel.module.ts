import { NgModule }                          from '@angular/core';
import { CommonModule }                      from '@angular/common';
import { RouterModule }                      from '@angular/router';
import { ReactiveFormsModule, FormsModule }  from '@angular/forms';

import { anfitrionGuard } from '../../../../guards/anfitrion.guard';
import { SharedModule }   from '../shared/shared.module';

// Página
import { PanelGestionPageComponent } from '../../pages/panel-gestion/panel-gestion';

/**
 * PanelModule — FIX
 *
 * Módulo exclusivo del panel de gestión del anfitrión.
 * ConfirmModalComponent viene de SharedModule — no se redeclara aquí.
 * ImageUploaderComponent no se usa en el panel — no se importa.
 *
 * Ruta: /mis-alojamientos → PanelGestionPageComponent
 */
@NgModule({
  declarations: [
    PanelGestionPageComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild([
      {
        path: '',
        canActivate: [anfitrionGuard],
        component: PanelGestionPageComponent
      }
    ])
  ]
})
export class PanelModule { }