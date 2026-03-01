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

  isLoading      = true;
  isSaving       = false;
  modoEdicion    = false;
  errorMessage   = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.perfilForm = this.fb.group({
      name:  ['', [Validators.required, Validators.maxLength(100)]],
      phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]]
    });

    this.cargarPerfil();
  }

  // ─── Getters del formulario ───────────────────────────────────
  get name()  { return this.perfilForm.get('name'); }
  get phone() { return this.perfilForm.get('phone'); }

  // ─── Cargar datos desde el backend ───────────────────────────
  cargarPerfil(): void {
    this.isLoading    = true;
    this.errorMessage = '';

    this.usuarioService.getMiPerfil().subscribe({
      next: (user) => {
        this.usuario   = user;
        this.isLoading = false;
        this.perfilForm.patchValue({
          name:  user.name,
          phone: user.phone ?? ''
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
    if (this.usuario) {
      this.perfilForm.patchValue({
        name:  this.usuario.name,
        phone: this.usuario.phone ?? ''
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
      name:  this.perfilForm.value.name.trim(),
      phone: this.perfilForm.value.phone.trim()
    };

    this.usuarioService.actualizarPerfil(datos).subscribe({
      next: (usuarioActualizado) => {
        this.usuario        = usuarioActualizado;
        this.isSaving       = false;
        this.modoEdicion    = false;
        this.successMessage = '¡Perfil actualizado correctamente!';
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
  getRolLabel(role: string | undefined): string {
    const labels: Record<string, string> = {
      USUARIO:   'Huésped',
      ANFITRION: 'Anfitrión',
      ADMIN:     'Administrador'
    };
    return labels[role ?? ''] ?? role ?? '';
  }

  getRolColor(role: string | undefined): string {
    const colors: Record<string, string> = {
      USUARIO:   'badge-guest',
      ANFITRION: 'badge-host',
      ADMIN:     'badge-admin'
    };
    return colors[role ?? ''] ?? '';
  }
}
