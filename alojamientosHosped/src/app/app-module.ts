import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideBrowserGlobalErrorListeners } from '@angular/core';

import { AppRoutingModule } from './app-routing-module';
import { App }              from './app';

// -- Interceptores
import { AuthInterceptor } from './interceptors/auth.interceptor';

/**
 * AppModule — INT-3 (Lazy Loading)
 *
 * Módulo raíz reducido al mínimo:
 * - Solo declara el componente raíz App
 * - Todos los componentes de páginas se mueven a feature modules
 *   que se cargan de forma diferida (lazy) según la ruta:
 *     · AuthModule         → /auth/**
 *     · AlojamientosModule → /alojamientos/**, /mis-alojamientos
 *     · UsuarioModule      → /perfil, /mis-favoritos, /mis-reservas
 *
 * Beneficio: el bundle inicial pasa de cargar ~40 componentes a solo
 * los estrictamente necesarios para arrancar la app.
 */
@NgModule({
  declarations: [
    App
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule
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