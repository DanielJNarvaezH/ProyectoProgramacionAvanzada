import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../../services/AuthService';
import { Router, RouterLink } from '@angular/router';

type Paso = 'email' | 'codigo';

@Component({
  selector: 'app-recuperar-contrasena',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './recuperar-contrasena.html',
  styleUrls: ['./recuperar-contrasena.scss']
})
export class RecuperarContrasenaComponent {

  paso: Paso = 'email';
  email = '';
  codigo = '';
  nuevaContrasena = '';
  confirmarContrasena = '';
  cargando = false;
  error = '';
  exito = '';

  constructor(private authService: AuthService, private router: Router) {}

  // ── Getters reactivos de requisitos ──────────────────────────
  get req8Chars(): boolean { return this.nuevaContrasena.length >= 8; }
  get reqMayuscula(): boolean { return /[A-Z]/.test(this.nuevaContrasena); }
  get reqNumero(): boolean { return /\d/.test(this.nuevaContrasena); }
  get reqEspecial(): boolean { return /[!@#$%^&*(),.?":{}|<>]/.test(this.nuevaContrasena); }
  get reqCoinciden(): boolean {
    return this.nuevaContrasena.length > 0 &&
           this.confirmarContrasena.length > 0 &&
           this.nuevaContrasena === this.confirmarContrasena;
  }
  get contrasenaValida(): boolean {
    return this.req8Chars && this.reqMayuscula && this.reqNumero && this.reqEspecial;
  }

  // ── Paso 1 ───────────────────────────────────────────────────
  solicitarCodigo(): void {
    if (!this.email) { this.error = 'Ingresa tu correo electrónico'; return; }
    this.cargando = true;
    this.error = '';
    this.authService.solicitarCodigo(this.email).subscribe({
      next: () => { this.cargando = false; this.paso = 'codigo'; this.exito = 'Código enviado a tu correo'; },
      error: (e) => { this.cargando = false; this.error = e.message; }
    });
  }

  // ── Paso 2 ───────────────────────────────────────────────────
  resetContrasena(): void {
    this.error = '';
    if (!this.codigo) { this.error = 'Ingresa el código recibido'; return; }
    if (!this.contrasenaValida) { this.error = 'La contraseña no cumple todos los requisitos'; return; }
    if (!this.reqCoinciden) { this.error = 'Las contraseñas no coinciden'; return; }
    this.cargando = true;
    this.authService.resetContrasena(this.email, this.codigo, this.nuevaContrasena).subscribe({
      next: () => {
        this.cargando = false;
        this.exito = '¡Contraseña restablecida exitosamente!';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (e) => { this.cargando = false; this.error = e.message; }
    });
  }

  volverAlLogin(): void { this.router.navigate(['/login']); }
}