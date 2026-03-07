import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginFormComponent } from './login-form';
import { AuthService } from '../../../../../services/AuthService';

// â”€â”€ Stubs â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

  // â”€â”€ CreaciÃ³n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('deberÃ­a crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // â”€â”€ ValidaciÃ³n inicial â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('el formulario deberÃ­a ser invÃ¡lido cuando estÃ¡ vacÃ­o', () => {
    expect(component.loginForm.invalid).toBeTrue();
  });

  it('el campo email deberÃ­a ser requerido', () => {
    component.emailCtrl.setValue('');
    expect(component.emailCtrl.hasError('required')).toBeTrue();
  });

  it('el campo email deberÃ­a rechazar un formato invÃ¡lido', () => {
    component.emailCtrl.setValue('no-es-un-email');
    expect(component.emailCtrl.hasError('email')).toBeTrue();
  });

  it('el campo email deberÃ­a aceptar un correo vÃ¡lido', () => {
    component.emailCtrl.setValue('usuario@ejemplo.com');
    expect(component.emailCtrl.valid).toBeTrue();
  });

  it('el campo password deberÃ­a ser requerido', () => {
    component.passwordCtrl.setValue('');
    expect(component.passwordCtrl.hasError('required')).toBeTrue();
  });

  it('el campo password deberÃ­a rechazar contraseÃ±as menores de 6 caracteres', () => {
    component.passwordCtrl.setValue('abc');
    expect(component.passwordCtrl.hasError('minlength')).toBeTrue();
  });

  it('el formulario deberÃ­a ser vÃ¡lido con datos correctos', () => {
    component.emailCtrl.setValue('usuario@ejemplo.com');
    component.passwordCtrl.setValue('segura123');
    expect(component.loginForm.valid).toBeTrue();
  });

  // â”€â”€ Mensajes de error â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('emailError deberÃ­a retornar null cuando el campo estÃ¡ pristine', () => {
    expect(component.emailError).toBeNull();
  });

  it('emailError deberÃ­a mostrar mensaje cuando el campo estÃ¡ tocado y vacÃ­o', () => {
    component.emailCtrl.setValue('');
    component.emailCtrl.markAsTouched();
    expect(component.emailError).toBe('El correo electrónico es obligatorio.');
  });

  it('passwordError deberÃ­a mostrar mensaje de minlength cuando la contraseÃ±a es corta', () => {
    component.passwordCtrl.setValue('123');
    component.passwordCtrl.markAsTouched();
    expect(component.passwordError).toBe('La contraseña debe tener al menos 6 caracteres.');
  });

  // â”€â”€ Toggle password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('togglePassword deberÃ­a alternar showPassword', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    component.togglePassword();
    expect(component.showPassword).toBeFalse();
  });

  // â”€â”€ onSubmit con formulario invÃ¡lido â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('onSubmit no deberÃ­a llamar a authService si el formulario es invÃ¡lido', () => {
    component.onSubmit();
    expect(authServiceStub.login).not.toHaveBeenCalled();
  });

  // â”€â”€ onSubmit con formulario vÃ¡lido (login exitoso) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('onSubmit deberÃ­a llamar a authService.login y navegar al inicio si tiene Ã©xito', () => {
    authServiceStub.login.and.returnValue(of({ token: 'abc', email: 'u@e.com', rol: 'USUARIO', mensaje: 'OK' }));

    component.emailCtrl.setValue('usuario@ejemplo.com');
    component.passwordCtrl.setValue('segura123');
    component.onSubmit();

    expect(authServiceStub.login).toHaveBeenCalledWith({
      email: 'usuario@ejemplo.com',
      password: 'segura123'
    });
    expect(routerStub.navigate).toHaveBeenCalledWith(['/alojamientos']);
    expect(component.isLoading).toBeFalse();
  });

  // â”€â”€ onSubmit con error del backend â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  it('onSubmit deberÃ­a mostrar errorMessage si el backend retorna un error', () => {
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
