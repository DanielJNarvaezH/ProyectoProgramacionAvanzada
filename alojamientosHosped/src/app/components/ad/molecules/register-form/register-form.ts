import { Component, OnInit, OnDestroy } from '@angular/core';
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
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-register-form',
  standalone: false,
  templateUrl: './register-form.html',
  styleUrls: ['./register-form.scss']
})
export class RegisterFormComponent implements OnInit, OnDestroy {

  registerForm!: FormGroup;
  isLoading           = false;
  errorMessage        = '';
  successMessage      = '';
  registroExitoso     = false;
  showPassword        = false;
  showConfirmPassword = false;

  // Errores de backend por campo para mostrarlos inline
  emailBackendError   = '';
  phoneBackendError   = '';

  private sub?: Subscription;

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
        Validators.pattern(/^[3][0-9]{9}$/)
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

    // Limpiar errores del backend al editar campos afectados
    this.sub = this.registerForm.get('email')!.valueChanges.subscribe(() => {
      if (this.emailBackendError) this.emailBackendError = '';
      if (this.errorMessage)      this.errorMessage = '';
    });
    this.registerForm.get('phone')!.valueChanges.subscribe(() => {
      if (this.phoneBackendError) this.phoneBackendError = '';
      if (this.errorMessage)      this.errorMessage = '';
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  // ─────────────────────────────────────────────────────────────────
  // VALIDADORES
  // ─────────────────────────────────────────────────────────────────

  private passwordSeguraValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    const tieneMayuscula = /[A-Z]/.test(value);
    const tieneNumero    = /[0-9]/.test(value);
    const tieneEspecial  = /[!@#$%^&*(),.?":{}|<>]/.test(value);
    if (!tieneMayuscula || !tieneNumero || !tieneEspecial) {
      return { passwordInsegura: true };
    }
    return null;
  }

  private mayorDeEdadValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    const hoy = new Date();
    const nacimiento = new Date(value);
    const edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();
    const edadReal = mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())
      ? edad - 1 : edad;
    return edadReal < 18 ? { menorDeEdad: true } : null;
  }

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
  // GETTERS
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

  irAlLogin(): void { this.router.navigate(['/login']); }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading         = true;
    this.errorMessage      = '';
    this.emailBackendError = '';
    this.phoneBackendError = '';

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
        this.isLoading       = false;
        this.registroExitoso = true;
        this.successMessage  = this.registerForm.value.name.trim().split(' ')[0];
        setTimeout(() => this.router.navigate(['/login']), 10000);
      },
      error: (err: any) => {
        this.isLoading = false;

        // Si el backend indicó el campo afectado, marcar inline
        if (err.campo === 'email') {
          this.emailBackendError = err.message;
          this.email?.setErrors({ backendError: true });
          this.email?.markAsTouched();
        } else if (err.campo === 'phone') {
          this.phoneBackendError = err.message;
          this.phone?.setErrors({ backendError: true });
          this.phone?.markAsTouched();
        } else {
          this.errorMessage = err.message || 'Error al registrar usuario';
        }
      }
    });
  }
}