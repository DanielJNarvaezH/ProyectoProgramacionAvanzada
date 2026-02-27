import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent }           from './components/ad/pages/login/login';
import { RegisterPageComponent }        from './components/ad/pages/register/register';
import { RecuperarContrasenaComponent } from './components/ad/pages/recuperar-contrasena/recuperar-contrasena';
import { PerfilPageComponent }          from './components/ad/pages/perfil/perfil'; // ← AUTH-21
import { authGuard }                    from './guards/auth.guard';

const routes: Routes = [
  { path: '',                    redirectTo: 'login', pathMatch: 'full' },
  { path: 'login',               component: LoginPageComponent },
  { path: 'register',            component: RegisterPageComponent },
  { path: 'recuperar-contrasena', component: RecuperarContrasenaComponent },
  // AUTH-21: ruta protegida del perfil
  { path: 'perfil',              component: PerfilPageComponent, canActivate: [authGuard] },
  // home protegido (pendiente de implementación futura)
  { path: 'home',                component: LoginPageComponent,  canActivate: [authGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
