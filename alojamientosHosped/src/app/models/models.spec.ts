import { LoginRequest } from './login-request.model';
import { RegisterRequest } from './register-request.model';
import { AuthResponse } from './auth-response.model';
import { User } from './user.model';

describe('Modelos TypeScript', () => {

  it('LoginRequest debe tener email y password', () => {
    const login: LoginRequest = {
      email: 'juan@example.com',
      password: 'Abc12345'
    };
    expect(login.email).toBe('juan@example.com');
    expect(login.password).toBe('Abc12345');
  });

  it('RegisterRequest debe tener todos los campos requeridos', () => {
    const register: RegisterRequest = {
      name: 'Juan Pérez',
      email: 'juan@example.com',
      phone: '3001234567',
      password: 'Abc12345',
      birthDate: '2000-05-15',
      role: 'USUARIO'
    };
    expect(register.name).toBe('Juan Pérez');
    expect(register.role).toBe('USUARIO');
  });

  it('AuthResponse debe contener token y datos del usuario', () => {
    const response: AuthResponse = {
      token: 'eyJhbGciOiJIUzI1NiJ9...',
      email: 'juan@example.com',
      rol: 'USUARIO',
      mensaje: 'Login exitoso'
    };
    expect(response.token).toBeTruthy();
    expect(response.rol).toBe('USUARIO');
  });

  it('User debe tener campos opcionales como undefined por defecto', () => {
    const user: User = {
      nombre: 'Juan Pérez',
      correo: 'juan@example.com',
      rol: 'ANFITRION'
    };
    expect(user.nombre).toBe('Juan Pérez');
    expect(user.id).toBeUndefined();
    expect(user.foto).toBeUndefined();
  });

});
