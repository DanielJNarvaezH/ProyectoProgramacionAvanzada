import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login/login'; // ✅ ruta corregida

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' }, // redirige a login
  { path: 'login', component: LoginPageComponent }      // carga tu página de login
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
