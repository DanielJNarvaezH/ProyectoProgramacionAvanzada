import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent }              from './components/ad/pages/login/login';
import { RegisterPageComponent }           from './components/ad/pages/register/register';
import { RecuperarContrasenaComponent }    from './components/ad/pages/recuperar-contrasena/recuperar-contrasena';
import { PerfilPageComponent }             from './components/ad/pages/perfil/perfil';
import { AlojamientosListaPageComponent }  from './components/ad/pages/alojamientos-lista/alojamientos-lista';
import { AlojamientoDetallePageComponent } from './components/ad/pages/alojamiento-detalle/alojamiento-detalle';
import { AlojamientoCrearPageComponent }   from './components/ad/pages/alojamiento-crear/alojamiento-crear';
import { authGuard }                       from './guards/auth.guard';
import { anfitrionGuard }                  from './guards/anfitrion.guard';

const routes: Routes = [
  { path: '',                     redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',                component: LoginPageComponent },
  { path: 'register',             component: RegisterPageComponent },
  { path: 'recuperar-contrasena', component: RecuperarContrasenaComponent },

  { path: 'perfil',               component: PerfilPageComponent,            canActivate: [authGuard] },
  { path: 'alojamientos',         component: AlojamientosListaPageComponent, canActivate: [authGuard] },

  // ALOJ-7: crear DEBE ir antes que :id — si va después Angular interpreta "crear" como un id
  { path: 'alojamientos/crear',   component: AlojamientoCrearPageComponent,  canActivate: [anfitrionGuard] },
  { path: 'alojamientos/:id',     component: AlojamientoDetallePageComponent, canActivate: [authGuard] },

  { path: 'home',                 component: LoginPageComponent,             canActivate: [authGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }