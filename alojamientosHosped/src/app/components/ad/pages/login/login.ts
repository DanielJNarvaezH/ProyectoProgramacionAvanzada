import { Component } from '@angular/core';

@Component({
  selector: 'app-login-page',
  standalone: false,
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class LoginPageComponent {
  email = '';
  password = '';

  onSubmit() {
    console.log('Correo:', this.email);
    console.log('Contraseña:', this.password);

    if (this.email === 'admin@example.com' && this.password === '123456') {
      alert('✅ Inicio de sesión exitoso');
      // Aquí podrías redirigir, por ejemplo:
      // this.router.navigate(['/dashboard']);
    } else {
      alert('❌ Credenciales incorrectas');
    }
  }
}



