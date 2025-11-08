import { Component } from '@angular/core';

@Component({
  selector: 'app-login-form',
  standalone: false,
  templateUrl: './login-form.html',
  styleUrls: ['./login-form.scss'],
})
export class LoginFormComponent {
  email: string = '';
  password: string = '';

  onSubmit() {
    if (this.email === 'admin@example.com' && this.password === '123456') {
      alert('✅ Inicio de sesión exitoso');
    } else {
      alert('❌ Credenciales incorrectas');
    }
  }
}
