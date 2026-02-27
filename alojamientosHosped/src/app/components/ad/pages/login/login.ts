import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../services/AuthService';

@Component({
  selector: 'app-login-page',
  standalone: false,
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class LoginPageComponent implements OnInit {

  loginForm!: FormGroup;
  isLoading    = false;
  errorMessage = '';
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  get email()    { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }

  togglePassword(): void { this.showPassword = !this.showPassword; }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading    = true;
    this.errorMessage = '';

    this.authService.login({
      email:    this.loginForm.value.email.trim().toLowerCase(),
      password: this.loginForm.value.password
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/perfil']); // ← único cambio
      },
      error: (err: Error) => {
        this.isLoading    = false;
        this.errorMessage = err.message || 'Credenciales incorrectas';
      }
    });
  }
}
