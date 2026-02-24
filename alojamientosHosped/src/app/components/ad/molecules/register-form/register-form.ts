import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../services/AuthService';
import { RegisterRequest } from '../../../../models';

/**
 * RegisterFormComponent — Molécula
 *
 * Formulario reactivo de registro con validaciones para:
 * - Nombre completo
 * - Email válido
 * - Teléfono colombiano (10 dígitos)
 * - Contraseña segura (mín. 8 chars, mayúscula, número, especial)
 * - Confirmar contraseña
 * - Fecha de nacimiento
 * - Rol (USUARIO o ANFITRION)
 */
@Component({
  selector: 'app-register-form',
  standalone: false,
  templateUrl: './register-form.html',
  styleUrls: ['./register-form.scss']
})
export class RegisterFormComponent implements OnInit {

  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      name: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
        Validators.pattern(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.maxLength(150)
      ]],
      phone: ['', [
        Validators.required,
        Validators.pattern(/^[3][0-9]{9}$/)   // Teléfono colombiano: empieza en 3, 10 dígitos
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        this.passwordSeguraValidator
      ]],
      confirmPassword: ['', Validators.required],
      birthDate: ['', [
        Validators.required,
        this.mayorDeEdadValidator
      ]],
      role: ['USUARIO', Validators.required]
    }, { validators: this.passwordsCoincidentesValidator });
  }

  // ─────────────────────────────────────────────────────────────────
  // VALIDADORES PERSONALIZADOS
  // ─────────────────────────────────────────────────────────────────

  /**
   * Valida que la contraseña tenga al menos:
   * - 1 letra mayúscula
   * - 1 número
   * - 1 carácter especial
   */
  private passwordSeguraValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const tieneMayuscula  = /[A-Z]/.test(value);
    const tieneNumero     = /[0-9]/.test(value);
    const tieneEspecial   = /[!@#$%^&*(),.?":{}|<>]/.test(value);

    if (!tieneMayuscula || !tieneNumero || !tieneEspecial) {
      return { passwordInsegura: true };
    }
    return null;
  }

  /**
   * Valida que el usuario sea mayor de 18 años
   */
  private mayorDeEdadValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const hoy = new Date();
    const nacimiento = new Date(value);
    const edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();
    const edadReal = mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())
      ? edad - 1
      : edad;

    return edadReal < 18 ? { menorDeEdad: true } : null;
  }

  /**
   * Valida que password y confirmPassword coincidan (validador de grupo)
   */
  private passwordsCoincidentesValidator(group: AbstractControl): ValidationErrors | null {
    const password        = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;

    if (password && confirmPassword && password !== confirmPassword) {
      group.get('confirmPassword')?.setErrors({ noCoinciden: true });
      return { noCoinciden: true };
    }
    return null;
  }

  // ─────────────────────────────────────────────────────────────────
  // GETTERS para acceso fácil en el template
  // ─────────────────────────────────────────────────────────────────

  get name()            { return this.registerForm.get('name'); }
  get email()           { return this.registerForm.get('email'); }
  get phone()           { return this.registerForm.get('phone'); }
  get password()        { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get birthDate()       { return this.registerForm.get('birthDate'); }
  get role()            { return this.registerForm.get('role'); }

  // ─────────────────────────────────────────────────────────────────
  // ACCIONES
  // ─────────────────────────────────────────────────────────────────

  togglePassword():        void { this.showPassword        = !this.showPassword; }
  toggleConfirmPassword(): void { this.showConfirmPassword = !this.showConfirmPassword; }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading    = true;
    this.errorMessage = '';

    const datos: RegisterRequest = {
      name:      this.registerForm.value.name.trim(),
      email:     this.registerForm.value.email.trim().toLowerCase(),
      phone:     this.registerForm.value.phone.trim(),
      password:  this.registerForm.value.password,
      birthDate: this.registerForm.value.birthDate,
      role:      this.registerForm.value.role
    };

    this.authService.register(datos).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: Error) => {
        this.isLoading    = false;
        this.errorMessage = err.message || 'Error al registrar usuario';
      }
    });
  }
}