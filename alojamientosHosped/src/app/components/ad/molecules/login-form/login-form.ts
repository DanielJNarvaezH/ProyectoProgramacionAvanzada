import { Component } from '@angular/core';

@Component({
  selector: 'app-login-form',
  standalone: false,
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
})
export class LoginFormComponent {
  email: string = '';
  password: string = '';

  onSubmit() {
    console.log('Datos de login:', { email: this.email, password: this.password });
  }
}
