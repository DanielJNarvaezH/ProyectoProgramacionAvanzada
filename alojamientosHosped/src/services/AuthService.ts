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
 * - logout()              → Limpia todos los datos de sesión en localStorage
 * - isAuthenticated()     → Verifica si hay un token de acceso válido
 * - solicitarCodigo()     → POST /api/auth/recuperar-contrasena
 * - resetContrasena()     → POST /api/auth/reset-contrasena
 *
 * Gestión de tokens (AUTH-19):
 * - getToken()            → Obtiene el access token
 * - getRefreshToken()     → Obtiene el refresh token
 * - guardarToken()        → Guarda solo el access token
 * - guardarRefreshToken() → Guarda solo el refresh token
 * - eliminarToken()       → Elimina solo el access token
 * - eliminarRefreshToken()→ Elimina solo el refresh token
 * - refreshAccessToken()  → POST /api/auth/refresh usando el refresh token
 * - getTokenExpiracion()  → Retorna la fecha de expiración del access token
 * - estaProximoAExpirar() → true si el token expira en menos de 5 minutos
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // ── Claves de localStorage ────────────────────────────────────────
  private readonly TOKEN_KEY         = 'hosped_token';
  private readonly REFRESH_TOKEN_KEY = 'hosped_refresh_token';
  private readonly USER_KEY          = 'hosped_user';

  // ── URL base del backend ──────────────────────────────────────────
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  // ─────────────────────────────────────────────────────────────────
  // LOGIN
  // ─────────────────────────────────────────────────────────────────

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => this.guardarSesion(response)),
      catchError(error => {
        const mensaje = error.error?.mensaje || 'Credenciales incorrectas';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // REGISTER
  // ─────────────────────────────────────────────────────────────────

  register(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, datos).pipe(
      tap(response => this.guardarSesion(response)),
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
   * Cierra la sesión eliminando todos los datos de autenticación
   * del localStorage: access token, refresh token y datos del usuario.
   */
  logout(): void {
    this.eliminarToken();
    this.eliminarRefreshToken();
    localStorage.removeItem(this.USER_KEY);
  }

  // ─────────────────────────────────────────────────────────────────
  // IS AUTHENTICATED
  // ─────────────────────────────────────────────────────────────────

  /**
   * Verifica si el usuario tiene una sesión activa con token válido.
   * Si el access token expiró pero hay refresh token disponible,
   * retorna false para que el interceptor pueda renovarlo.
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirado = payload.exp * 1000 < Date.now();
      if (expirado) {
        this.eliminarToken(); // Solo elimina el access token, mantiene el refresh
        return false;
      }
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  // ─────────────────────────────────────────────────────────────────
  // GESTIÓN DE ACCESS TOKEN
  // ─────────────────────────────────────────────────────────────────

  /**
   * Retorna el access token JWT almacenado o null si no existe.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Guarda únicamente el access token en localStorage.
   * Útil cuando el backend renueva solo el access token.
   *
   * @param token - Nuevo access token JWT
   */
  guardarToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Elimina únicamente el access token del localStorage.
   * No afecta el refresh token ni los datos del usuario.
   */
  eliminarToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  // ─────────────────────────────────────────────────────────────────
  // GESTIÓN DE REFRESH TOKEN
  // ─────────────────────────────────────────────────────────────────

  /**
   * Retorna el refresh token almacenado o null si no existe.
   * El refresh token tiene mayor duración que el access token.
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Guarda únicamente el refresh token en localStorage.
   *
   * @param refreshToken - Refresh token recibido del backend
   */
  guardarRefreshToken(refreshToken: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  /**
   * Elimina únicamente el refresh token del localStorage.
   * Se usa cuando el refresh token expira o es inválido.
   */
  eliminarRefreshToken(): void {
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Solicita al backend un nuevo access token usando el refresh token.
   * Si el refresh token no existe o el backend lo rechaza, hace logout.
   *
   * Endpoint: POST /api/auth/refresh
   * Body: { refreshToken: string }
   * Response: { token: string, refreshToken: string, ... }
   *
   * @returns Observable<AuthResponse> con el nuevo access token
   */
  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No hay refresh token disponible'));
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        // Actualizar solo los tokens, mantener datos del usuario
        this.guardarToken(response.token);
        if (response.refreshToken) {
          this.guardarRefreshToken(response.refreshToken);
        }
      }),
      catchError(error => {
        // Si el refresh falla, cerrar sesión completamente
        this.logout();
        const mensaje = error.error?.mensaje || 'Sesión expirada, inicia sesión nuevamente';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // UTILIDADES DE EXPIRACIÓN
  // ─────────────────────────────────────────────────────────────────

  /**
   * Retorna la fecha de expiración del access token actual.
   * Útil para mostrar al usuario cuánto tiempo le queda de sesión.
   *
   * @returns Date de expiración o null si no hay token
   */
  getTokenExpiracion(): Date | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return new Date(payload.exp * 1000);
    } catch {
      return null;
    }
  }

  /**
   * Retorna true si el access token expira en menos de 5 minutos.
   * El interceptor puede usar esto para renovar el token de forma proactiva
   * antes de que expire y evitar errores 401.
   */
  estaProximoAExpirar(): boolean {
    const expiracion = this.getTokenExpiracion();
    if (!expiracion) return false;

    const cincoMinutos = 5 * 60 * 1000;
    return expiracion.getTime() - Date.now() < cincoMinutos;
  }

  // ─────────────────────────────────────────────────────────────────
  // RECUPERAR CONTRASEÑA
  // ─────────────────────────────────────────────────────────────────

  solicitarCodigo(email: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/recuperar-contrasena`,
      { email },
      { responseType: 'text' }
    ).pipe(
      catchError(error => {
        let mensaje: string;

        if (error.status === 0) {
          mensaje = 'No se puede conectar con el servidor. Verifica que el backend esté corriendo.';
        } else if (error.status === 404) {
          mensaje = 'El correo no está registrado en el sistema.';
        } else {
          mensaje = error.error || 'Error al enviar el código';
        }

        return throwError(() => new Error(mensaje));
      })
    );
  }

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

  getUsuario(): Partial<User> | null {
    const data = localStorage.getItem(this.USER_KEY);
    return data ? JSON.parse(data) : null;
  }

  getRol(): string | null {
    return this.getUsuario()?.rol ?? null;
  }

  esAnfitrion(): boolean {
    return this.getRol() === 'ANFITRION';
  }

  esAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  // ─────────────────────────────────────────────────────────────────
  // UTILIDADES PRIVADAS
  // ─────────────────────────────────────────────────────────────────

  /**
   * Guarda access token, refresh token y datos del usuario en localStorage.
   * Se llama automáticamente tras login y register exitosos.
   */
  private guardarSesion(response: AuthResponse): void {
    this.guardarToken(response.token);
    if (response.refreshToken) {
      this.guardarRefreshToken(response.refreshToken);
    }
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      correo: response.email,
      rol:    response.rol
    }));
  }
}
