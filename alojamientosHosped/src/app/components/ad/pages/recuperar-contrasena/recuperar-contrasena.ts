import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../services/AuthService';
import { Router, RouterLink } from '@angular/router';


type Paso = 'email' | 'codigo';

@Component({
  selector: 'app-recuperar-contrasena',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],  // ← agregar RouterLink
  templateUrl: './recuperar-contrasena.html',
  styleUrls: ['./recuperar-contrasena.scss']         // ← agregar esta línea

})
export class RecuperarContrasenaComponent {

  paso: Paso = 'email';

  // Paso 1
  email = '';

  // Paso 2
  codigo = '';
  nuevaContrasena = '';
  confirmarContrasena = '';

  // Estado UI
  cargando = false;
  error = '';
  exito = '';

  constructor(private authService: AuthService, private router: Router) {}

  // ── Paso 1: solicitar código ──────────────────────────────────

  solicitarCodigo(): void {
    if (!this.email) {
      this.error = 'Ingresa tu correo electrónico';
      return;
    }
    this.cargando = true;
    this.error = '';

    this.authService.solicitarCodigo(this.email).subscribe({
      next: () => {
        this.cargando = false;
        this.paso = 'codigo';
        this.exito = 'Código enviado a tu correo';
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.message;
      }
    });
  }

  // ── Paso 2: resetear contraseña ───────────────────────────────

  resetContrasena(): void {
    this.error = '';

    if (!this.codigo) {
      this.error = 'Ingresa el código recibido';
      return;
    }
    if (this.nuevaContrasena.length < 8) {
      this.error = 'La contraseña debe tener al menos 8 caracteres';
      return;
    }
    if (!/[A-Z]/.test(this.nuevaContrasena)) {
      this.error = 'La contraseña debe tener al menos una mayúscula';
      return;
    }
    if (!/\d/.test(this.nuevaContrasena)) {
      this.error = 'La contraseña debe tener al menos un número';
      return;
    }
    if (this.nuevaContrasena !== this.confirmarContrasena) {
      this.error = 'Las contraseñas no coinciden';
      return;
    }

    this.cargando = true;

    this.authService.resetContrasena(this.email, this.codigo, this.nuevaContrasena).subscribe({
      next: () => {
        this.cargando = false;
        this.exito = '¡Contraseña restablecida exitosamente!';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.message;
      }
    });
  }

  volverAlLogin(): void {
    this.router.navigate(['/login']);
  }
}
