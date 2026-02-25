import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  User
} from '../app/models';

/**
 * AuthService — Servicio central de autenticación para la plataforma Hosped.
 *
 * Gestiona:
 * - login()               → POST /api/auth/login
 * - register()            → POST /api/auth/register
 * - logout()              → Limpia el token y datos del usuario en localStorage
 * - isAuthenticated()     → Verifica si hay un token válido almacenado
 * - solicitarCodigo()     → POST /api/auth/recuperar-contrasena
 * - resetContrasena()     → POST /api/auth/reset-contrasena
 *
 * El token JWT y los datos del usuario se almacenan en localStorage
 * para persistir la sesión entre recargas de página.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // ── Claves de localStorage ────────────────────────────────────────
  private readonly TOKEN_KEY = 'hosped_token';
  private readonly USER_KEY  = 'hosped_user';

  // ── URL base del backend ──────────────────────────────────────────
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // LOGIN
  // ─────────────────────────────────────────────────────────────────

  /**
   * Inicia sesión con email y contraseña.
   * Al recibir respuesta exitosa, guarda el token JWT y los datos
   * del usuario en localStorage automáticamente.
   *
   * @param credentials - Email y contraseña del usuario
   * @returns Observable<AuthResponse> con token, email, rol y mensaje
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        this.guardarSesion(response);
      }),
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Credenciales incorrectas';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // REGISTER
  // ─────────────────────────────────────────────────────────────────

  /**
   * Registra un nuevo usuario en la plataforma.
   * Permite registrar usuarios con rol USUARIO o ANFITRION.
   *
   * @param datos - Datos de registro: nombre, email, teléfono, contraseña, fecha nacimiento, rol
   * @returns Observable<AuthResponse> con token y datos del usuario registrado
   */
  register(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, datos).pipe(
      tap(response => {
        this.guardarSesion(response);
      }),
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Error al registrar usuario';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // LOGOUT
  // ─────────────────────────────────────────────────────────────────

  /**
   * Cierra la sesión del usuario eliminando el token JWT
   * y los datos almacenados en localStorage.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  // ─────────────────────────────────────────────────────────────────
  // IS AUTHENTICATED
  // ─────────────────────────────────────────────────────────────────

  /**
   * Verifica si el usuario tiene una sesión activa válida.
   * Comprueba que exista un token en localStorage y que no haya expirado.
   *
   * @returns true si el usuario está autenticado, false en caso contrario
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    // Verificar expiración decodificando el payload del JWT
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirado = payload.exp * 1000 < Date.now();
      if (expirado) {
        this.logout(); // Limpiar sesión expirada automáticamente
        return false;
      }
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // RECUPERAR CONTRASEÑA
  // ─────────────────────────────────────────────────────────────────

  /**
   * Envía un código de recuperación al email del usuario.
   *
   * @param email - Correo del usuario registrado
   * @returns Observable<string> con mensaje de confirmación
   */
  solicitarCodigo(email: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/recuperar-contrasena`,
      { email },
      { responseType: 'text' }
    ).pipe(
      catchError(error => {
        const mensaje = error.error || 'Error al enviar el código';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  /**
   * Restablece la contraseña usando el código recibido por email.
   *
   * @param email           - Correo del usuario
   * @param codigo          - Código recibido por email
   * @param nuevaContrasena - Nueva contraseña
   * @returns Observable<string> con mensaje de confirmación
   */
  resetContrasena(email: string, codigo: string, nuevaContrasena: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/reset-contrasena`,
      { email, codigo, nuevaContrasena },
      { responseType: 'text' }
    ).pipe(
      catchError(error => {
        const mensaje = error.error || 'Error al restablecer la contraseña';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // GETTERS DE SESIÓN
  // ─────────────────────────────────────────────────────────────────

  /**
   * Retorna el token JWT almacenado o null si no existe.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Retorna los datos del usuario autenticado o null si no existe sesión.
   */
  getUsuario(): Partial<User> | null {
    const data = localStorage.getItem(this.USER_KEY);
    return data ? JSON.parse(data) : null;
  }

  /**
   * Retorna el rol del usuario autenticado o null si no existe sesión.
   * Posibles valores: 'USUARIO' | 'ANFITRION' | 'ADMIN'
   */
  getRol(): string | null {
    return this.getUsuario()?.rol ?? null;
  }

  /**
   * Retorna true si el usuario autenticado tiene rol ANFITRION.
   */
  esAnfitrion(): boolean {
    return this.getRol() === 'ANFITRION';
  }

  /**
   * Retorna true si el usuario autenticado tiene rol ADMIN.
   */
  esAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  // ─────────────────────────────────────────────────────────────────
  // UTILIDADES PRIVADAS
  // ─────────────────────────────────────────────────────────────────

  /**
   * Guarda el token JWT y los datos básicos del usuario en localStorage.
   */
  private guardarSesion(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      correo: response.email,
      rol:    response.rol
    }));
  }
}
