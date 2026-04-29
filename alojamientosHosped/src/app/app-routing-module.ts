import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard }      from './guards/auth.guard';
import { anfitrionGuard } from './guards/anfitrion.guard';

/**
 * AppRoutingModule — INT-3 (Lazy Loading)
 *
 * FIX: /mis-alojamientos ahora carga PanelModule (módulo propio)
 * en vez de reutilizar AlojamientosModule con path:'' que siempre
 * mostraba el listado de alojamientos en lugar del panel de gestión.
 */
const routes: Routes = [
  // Redirección raíz
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  // ── Auth (login, register, recuperar-contrasena) ──────────────────────
  {
    path: 'auth',
    loadChildren: () =>
      import('./components/ad/modules/auth/auth.module')
        .then(m => m.AuthModule)
  },

  // Alias directos para compatibilidad con links existentes
  { path: 'login',                redirectTo: 'auth/login',                pathMatch: 'full' },
  { path: 'register',             redirectTo: 'auth/register',             pathMatch: 'full' },
  { path: 'recuperar-contrasena', redirectTo: 'auth/recuperar-contrasena', pathMatch: 'full' },

  // ── Alojamientos (listado + detalle + crear + editar) ─────────────────
  {
    path: 'alojamientos',
    loadChildren: () =>
      import('./components/ad/modules/alojamientos/alojamientos.module')
        .then(m => m.AlojamientosModule),
    canActivate: [authGuard]
  },

  // ── Panel de gestión del anfitrión — módulo propio ────────────────────
  // FIX: ruta separada que carga PanelModule con path:'' → PanelGestionPageComponent
  {
    path: 'mis-alojamientos',
    loadChildren: () =>
      import('./components/ad/modules/panel/panel.module')
        .then(m => m.PanelModule),
    canActivate: [anfitrionGuard]
  },

  // ── Área personal del usuario ─────────────────────────────────────────
  {
    path: '',
    loadChildren: () =>
      import('./components/ad/modules/usuario/usuario.module')
        .then(m => m.UsuarioModule),
    canActivate: [authGuard]
  },

  // Ruta home → alias de login
  { path: 'home', redirectTo: 'auth/login', pathMatch: 'full' },

  // Wildcard → redirigir a login
  { path: '**', redirectTo: 'auth/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    preloadingStrategy: PreloadAllModules
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

// Necesario para preloadingStrategy
import { PreloadAllModules } from '@angular/router';