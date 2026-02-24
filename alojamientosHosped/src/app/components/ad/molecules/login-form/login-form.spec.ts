import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginFormComponent } from './login-form';
import { AuthService } from '../../../../../services/AuthService';

// ── Stubs ──────────────────────────────────────────────────────────
const authServiceStub = {
  login: jasmine.createSpy('login')
};
const routerStub = {
  navigate: jasmine.createSpy('navigate')
};

describe('LoginFormComponent', () => {
  let component: LoginFormComponent;
  let fixture: ComponentFixture<LoginFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginFormComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        { provide: Router,      useValue: routerStub }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(LoginFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    authServiceStub.login.calls.reset();
    routerStub.navigate.calls.reset();
  });

  // ── Creación ──────────────────────────────────────────────────────

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // ── Validación inicial ────────────────────────────────────────────

  it('el formulario debería ser inválido cuando está vacío', () => {
    expect(component.loginForm.invalid).toBeTrue();
  });

  it('el campo email debería ser requerido', () => {
    component.emailCtrl.setValue('');
    expect(component.emailCtrl.hasError('required')).toBeTrue();
  });

  it('el campo email debería rechazar un formato inválido', () => {
    component.emailCtrl.setValue('no-es-un-email');
    expect(component.emailCtrl.hasError('email')).toBeTrue();
  });

  it('el campo email debería aceptar un correo válido', () => {
    component.emailCtrl.setValue('usuario@ejemplo.com');
    expect(component.emailCtrl.valid).toBeTrue();
  });

  it('el campo password debería ser requerido', () => {
    component.passwordCtrl.setValue('');
    expect(component.passwordCtrl.hasError('required')).toBeTrue();
  });

  it('el campo password debería rechazar contraseñas menores de 6 caracteres', () => {
    component.passwordCtrl.setValue('abc');
    expect(component.passwordCtrl.hasError('minlength')).toBeTrue();
  });

  it('el formulario debería ser válido con datos correctos', () => {
    component.emailCtrl.setValue('usuario@ejemplo.com');
    component.passwordCtrl.setValue('segura123');
    expect(component.loginForm.valid).toBeTrue();
  });

  // ── Mensajes de error ─────────────────────────────────────────────

  it('emailError debería retornar null cuando el campo está pristine', () => {
    expect(component.emailError).toBeNull();
  });

  it('emailError debería mostrar mensaje cuando el campo está tocado y vacío', () => {
    component.emailCtrl.setValue('');
    component.emailCtrl.markAsTouched();
    expect(component.emailError).toBe('El correo electrónico es obligatorio.');
  });

  it('passwordError debería mostrar mensaje de minlength cuando la contraseña es corta', () => {
    component.passwordCtrl.setValue('123');
    component.passwordCtrl.markAsTouched();
    expect(component.passwordError).toBe('La contraseña debe tener al menos 6 caracteres.');
  });

  // ── Toggle password ───────────────────────────────────────────────

  it('togglePassword debería alternar showPassword', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    component.togglePassword();
    expect(component.showPassword).toBeFalse();
  });

  // ── onSubmit con formulario inválido ──────────────────────────────

  it('onSubmit no debería llamar a authService si el formulario es inválido', () => {
    component.onSubmit();
    expect(authServiceStub.login).not.toHaveBeenCalled();
  });

  // ── onSubmit con formulario válido (login exitoso) ────────────────

  it('onSubmit debería llamar a authService.login y navegar al inicio si tiene éxito', () => {
    authServiceStub.login.and.returnValue(of({ token: 'abc', email: 'u@e.com', rol: 'USUARIO', mensaje: 'OK' }));

    component.emailCtrl.setValue('usuario@ejemplo.com');
    component.passwordCtrl.setValue('segura123');
    component.onSubmit();

    expect(authServiceStub.login).toHaveBeenCalledWith({
      email: 'usuario@ejemplo.com',
      password: 'segura123'
    });
    expect(routerStub.navigate).toHaveBeenCalledWith(['/']);
    expect(component.isLoading).toBeFalse();
  });

  // ── onSubmit con error del backend ────────────────────────────────

  it('onSubmit debería mostrar errorMessage si el backend retorna un error', () => {
    authServiceStub.login.and.returnValue(
      throwError(() => new Error('Credenciales incorrectas'))
    );

    component.emailCtrl.setValue('usuario@ejemplo.com');
    component.passwordCtrl.setValue('segura123');
    component.onSubmit();

    expect(component.errorMessage).toBe('Credenciales incorrectas');
    expect(component.isLoading).toBeFalse();
    expect(routerStub.navigate).not.toHaveBeenCalled();
  });
});
