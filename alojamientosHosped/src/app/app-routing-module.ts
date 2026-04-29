import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard }      from './guards/auth.guard';
import { anfitrionGuard } from './guards/anfitrion.guard';

/**
 * AppRoutingModule — INT-3 (Lazy Loading)
 *
 * Rutas principales de la aplicación con lazy loading de feature modules.
 *
 * Antes: todas las páginas en un solo AppModule → bundle inicial grande.
 * Ahora: 3 feature modules cargados solo cuando el usuario navega a ellos:
 *
 *   AuthModule        → /auth/** (login, register, recuperar-contrasena)
 *   AlojamientosModule → /alojamientos/**, /mis-alojamientos
 *   UsuarioModule     → /perfil, /mis-favoritos, /mis-reservas
 *
 * Cada loadChildren usa import() dinámico → Webpack genera un chunk
 * separado por módulo que el browser descarga solo cuando lo necesita.
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

  // ── Alojamientos ──────────────────────────────────────────────────────
  {
    path: 'alojamientos',
    loadChildren: () =>
      import('./components/ad/modules/alojamientos/alojamientos.module')
        .then(m => m.AlojamientosModule),
    canActivate: [authGuard]
  },

  // Panel de gestión del anfitrión (dentro del módulo Alojamientos)
  {
    path: 'mis-alojamientos',
    loadChildren: () =>
      import('./components/ad/modules/alojamientos/alojamientos.module')
        .then(m => m.AlojamientosModule),
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
    // Precargar todos los módulos lazy después del arranque inicial
    // para que la segunda navegación sea instantánea
    preloadingStrategy: PreloadAllModules
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

// Necesario para preloadingStrategy
import { PreloadAllModules } from '@angular/router';