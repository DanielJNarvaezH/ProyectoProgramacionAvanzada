import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../services/AuthService';

/**
 * NavbarComponent — Organismo reutilizable
 *
 * Barra de navegación superior de Hosped.
 * - Logo con enlace a /alojamientos
 * - Botón "Mi panel" visible SOLO para anfitriones (ALOJ-9)
 * - Botón "Publicar" visible SOLO para anfitriones (ALOJ-7)
 * - Botón de perfil para todos los usuarios autenticados
 */
@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class NavbarComponent {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /** Primer nombre del usuario para el tooltip del ícono */
  get nombreUsuario(): string {
    const usuario = this.authService.getUsuario();
    return usuario?.name?.split(' ')[0] || 'Mi perfil';
  }

  /** ALOJ-7: solo anfitriones ven el botón Publicar */
  get esAnfitrion(): boolean {
    return this.authService.esAnfitrion();
  }

  irAlPerfil(): void {
    this.router.navigate(['/perfil']);
  }

  irACrear(): void {
    this.router.navigate(['/alojamientos/crear']);
  }

  /** ALOJ-9: navega al panel de gestión del anfitrión */
  irAlPanel(): void {
    this.router.navigate(['/mis-alojamientos']);
  }
}
