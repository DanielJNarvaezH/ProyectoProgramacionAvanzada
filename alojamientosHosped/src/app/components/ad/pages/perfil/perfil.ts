import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UsuarioService } from '../../../../../services/UsuarioService';
import { AuthService } from '../../../../../services/AuthService';
import { User } from '../../../../models';

@Component({
  selector: 'app-perfil-page',
  standalone: false,
  templateUrl: './perfil.html',
  styleUrls: ['./perfil.scss']
})
export class PerfilPageComponent implements OnInit {

  usuario: User | null = null;
  perfilForm!: FormGroup;

  isLoading    = true;   // cargando datos iniciales
  isSaving     = false;  // guardando cambios
  modoEdicion  = false;  // alterna vista/edición
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.perfilForm = this.fb.group({
      nombre:   ['', [Validators.required, Validators.maxLength(100)]],
      telefono: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]]
    });

    this.cargarPerfil();
  }

  // ─── Getters del formulario ───────────────────────────────────
  get nombre()   { return this.perfilForm.get('nombre'); }
  get telefono() { return this.perfilForm.get('telefono'); }

  // ─── Cargar datos desde el backend ───────────────────────────
  cargarPerfil(): void {
    this.isLoading    = true;
    this.errorMessage = '';

    this.usuarioService.getMiPerfil().subscribe({
      next: (user) => {
        this.usuario  = user;
        this.isLoading = false;
        this.perfilForm.patchValue({
          nombre:   user.nombre,
          telefono: user.telefono ?? ''
        });
      },
      error: (err: Error) => {
        this.isLoading    = false;
        this.errorMessage = err.message;
      }
    });
  }

  // ─── Activar modo edición ─────────────────────────────────────
  activarEdicion(): void {
    this.modoEdicion    = true;
    this.successMessage = '';
    this.errorMessage   = '';
  }

  // ─── Cancelar edición ─────────────────────────────────────────
  cancelarEdicion(): void {
    this.modoEdicion  = false;
    this.errorMessage = '';
    // Restaurar valores originales
    if (this.usuario) {
      this.perfilForm.patchValue({
        nombre:   this.usuario.nombre,
        telefono: this.usuario.telefono ?? ''
      });
    }
  }

  // ─── Guardar cambios ──────────────────────────────────────────
  guardarCambios(): void {
    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    this.isSaving     = true;
    this.errorMessage = '';

    const datos: Partial<User> = {
      nombre:   this.perfilForm.value.nombre.trim(),
      telefono: this.perfilForm.value.telefono.trim()
    };

    this.usuarioService.actualizarPerfil(datos).subscribe({
      next: (usuarioActualizado) => {
        this.usuario        = usuarioActualizado;
        this.isSaving       = false;
        this.modoEdicion    = false;
        this.successMessage = '¡Perfil actualizado correctamente!';
        // Limpiar el mensaje de éxito después de 3 segundos
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err: Error) => {
        this.isSaving     = false;
        this.errorMessage = err.message;
      }
    });
  }

  // ─── Cerrar sesión ────────────────────────────────────────────
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // ─── Helpers de UI ───────────────────────────────────────────
  getRolLabel(rol: string | undefined): string {
    const labels: Record<string, string> = {
      USUARIO:   'Huésped',
      ANFITRION: 'Anfitrión',
      ADMIN:     'Administrador'
    };
    return labels[rol ?? ''] ?? rol ?? '';
  }

  getRolColor(rol: string | undefined): string {
    const colors: Record<string, string> = {
      USUARIO:   'badge-guest',
      ANFITRION: 'badge-host',
      ADMIN:     'badge-admin'
    };
    return colors[rol ?? ''] ?? '';
  }
}
