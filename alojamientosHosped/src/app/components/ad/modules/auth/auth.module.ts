import { NgModule }                          from '@angular/core';
import { CommonModule }                      from '@angular/common';
import { RouterModule }                      from '@angular/router';
import { FormsModule, ReactiveFormsModule }  from '@angular/forms';

// -- SharedModule (aporta: IconComponent y otros compartidos)
import { SharedModule } from '../shared/shared.module';

// -- Atomos exclusivos de Auth
import { ButtonComponent } from '../../atoms/button/button';
import { LabelComponent }  from '../../atoms/label/label';
import { InputComponent }  from '../../atoms/input/input';

// -- Moléculas
import { InputFieldComponent }    from '../../molecules/input-field/input-field';
import { PasswordFieldComponent } from '../../molecules/password-field/password-field';
import { LoginFormComponent }     from '../../molecules/login-form/login-form';
import { RegisterFormComponent }  from '../../molecules/register-form/register-form';

// -- Organismos
import { LoginCardComponent }    from '../../organisms/login-card/login-card';
import { RegisterCardComponent } from '../../organisms/register-card/register-card';

// -- Templates
import { LoginTemplateComponent }    from '../../templates/login-template/login-template';
import { RegisterTemplateComponent } from '../../templates/register-template/register-template';

// -- Páginas
import { LoginPageComponent }    from '../../pages/login/login';
import { RegisterPageComponent } from '../../pages/register/register';

// standalone: true → va en imports[], no en declarations[]
import { RecuperarContrasenaComponent } from '../../pages/recuperar-contrasena/recuperar-contrasena';

/**
 * AuthModule — INT-3 (Lazy Loading)
 * FormsModule incluido porque InputComponent usa ngModel.
 * IconComponent viene de SharedModule — no se redeclara aquí.
 */
@NgModule({
  declarations: [
    ButtonComponent,
    LabelComponent,
    InputComponent,
    InputFieldComponent,
    PasswordFieldComponent,
    LoginFormComponent,
    RegisterFormComponent,
    LoginCardComponent,
    RegisterCardComponent,
    LoginTemplateComponent,
    RegisterTemplateComponent,
    LoginPageComponent,
    RegisterPageComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,            // ← necesario para ngModel en InputComponent
    ReactiveFormsModule,
    SharedModule,
    RecuperarContrasenaComponent,
    RouterModule.forChild([
      { path: '',                     redirectTo: 'login', pathMatch: 'full' },
      { path: 'login',                component: LoginPageComponent },
      { path: 'register',             component: RegisterPageComponent },
      { path: 'recuperar-contrasena', component: RecuperarContrasenaComponent },
    ])
  ]
})
export class AuthModule { }