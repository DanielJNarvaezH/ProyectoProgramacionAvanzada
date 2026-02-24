import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent } from './components/ad/pages/login/login';
import { authGuard } from './guards/auth.guard';
import { RegisterPageComponent } from './components/ad/pages/register/register'; 

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'home', component: LoginPageComponent, canActivate: [authGuard] }, // ← ruta protegida de ejemplo
  { path: 'register', component: RegisterPageComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }