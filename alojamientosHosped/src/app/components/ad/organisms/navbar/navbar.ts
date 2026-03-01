import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../services/AuthService';

/**
 * NavbarComponent — Organismo reutilizable
 *
 * Barra de navegación superior de Hosped.
 * Muestra el logo y el ícono de perfil del usuario autenticado.
 * Al hacer clic en el ícono navega a /perfil.
 *
 * Uso:
 * <app-navbar></app-navbar>
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

  irAlPerfil(): void {
    this.router.navigate(['/perfil']);
  }
}