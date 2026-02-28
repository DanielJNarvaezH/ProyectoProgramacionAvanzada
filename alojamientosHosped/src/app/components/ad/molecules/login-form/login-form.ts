import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../services/AuthService';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-login-form',
  standalone: false,
  templateUrl: './login-form.html',
  styleUrls: ['./login-form.scss'],
})
export class LoginFormComponent implements OnInit, OnDestroy {

  loginForm!: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';
  showPassword: boolean = false;
  private sub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(100)
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6),
          Validators.maxLength(50)
        ]
      ]
    });

    // Limpiar el error del backend cuando el usuario empiece a corregir
    this.sub = this.loginForm.valueChanges.subscribe(() => {
      if (this.errorMessage) {
        this.errorMessage = '';
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  get emailCtrl() {
    return this.loginForm.get('email')!;
  }

  get passwordCtrl() {
    return this.loginForm.get('password')!;
  }

  get emailError(): string | null {
    const ctrl = this.emailCtrl;
    if (ctrl.untouched || ctrl.valid) return null;
    if (ctrl.hasError('required'))  return 'El correo electrónico es obligatorio.';
    if (ctrl.hasError('email'))     return 'Ingresa un correo electrónico válido (ej: usuario@ejemplo.com).';
    if (ctrl.hasError('maxlength')) return 'El correo no puede superar 100 caracteres.';
    return null;
  }

  get passwordError(): string | null {
    const ctrl = this.passwordCtrl;
    if (ctrl.untouched || ctrl.valid) return null;
    if (ctrl.hasError('required'))   return 'La contraseña es obligatoria.';
    if (ctrl.hasError('minlength'))  return 'La contraseña debe tener al menos 6 caracteres.';
    if (ctrl.hasError('maxlength'))  return 'La contraseña no puede superar 50 caracteres.';
    return null;
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) return;

    this.isLoading    = true;
    this.errorMessage = '';

    const credentials = {
      email:    this.emailCtrl.value.trim(),
      password: this.passwordCtrl.value
    };

    this.authService.login(credentials).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/alojamientos']);
      },
      error: (err: Error) => {
        this.isLoading    = false;
        this.errorMessage = err.message || 'Ocurrió un error al iniciar sesión. Intenta de nuevo.';
      }
    });
  }
}
