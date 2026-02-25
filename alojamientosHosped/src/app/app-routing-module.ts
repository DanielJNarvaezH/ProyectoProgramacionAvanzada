import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent } from './components/ad/pages/login/login';
import { authGuard } from './guards/auth.guard';
import { RegisterPageComponent } from './components/ad/pages/register/register';
import { RecuperarContrasenaComponent } from './components/ad/pages/recuperar-contrasena/recuperar-contrasena';


const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'home', component: LoginPageComponent, canActivate: [authGuard] },
  { path: 'register', component: RegisterPageComponent },
  { path: 'recuperar-contrasena', component: RecuperarContrasenaComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
