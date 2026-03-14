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
        const mensaje = this.resolverErrorLogin(error);
        return throwError(() => new Error(mensaje));
      })
    );
  }

  private resolverErrorLogin(error: any): string {
    if (error.status === 0) {
      return 'No se puede conectar con el servidor. Verifica tu conexión a internet.';
    }
    const backendMsg: string = typeof error.error === 'string'
      ? error.error
      : (error.error?.mensaje || error.error?.message || '');

    if (error.status === 401) {
      return backendMsg || 'Correo o contraseña incorrectos. Verifica tus datos e intenta de nuevo.';
    }
    if (error.status === 403) {
      return backendMsg || 'Tu cuenta está desactivada o bloqueada. Contacta al soporte.';
    }
    if (error.status === 429) {
      return 'Demasiados intentos fallidos. Espera unos minutos antes de intentar de nuevo.';
    }
    if (error.status >= 500) {
      return 'Error interno del servidor. Intenta de nuevo más tarde.';
    }
    return backendMsg || 'Correo o contraseña incorrectos.';
  }

  // ─────────────────────────────────────────────────────────────────
  // REGISTER
  // ─────────────────────────────────────────────────────────────────

  register(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, datos).pipe(
      tap(response => this.guardarSesion(response)),
      catchError(error => {
        const { mensaje, campo } = this.resolverErrorRegister(error);
        const err: any = new Error(mensaje);
        err.campo = campo;
        return throwError(() => err);
      })
    );
  }

  private resolverErrorRegister(error: any): { mensaje: string; campo?: string } {
    if (error.status === 0) {
      return { mensaje: 'No se puede conectar con el servidor. Verifica tu conexión a internet.' };
    }
    const backendMsg: string = typeof error.error === 'string'
      ? error.error
      : (error.error?.mensaje || error.error?.message || '');

    if (error.status === 409) {
      return { mensaje: 'Este correo electrónico ya está registrado. ¿Olvidaste tu contraseña?', campo: 'email' };
    }
    if (error.status === 400) {
      if (backendMsg.toLowerCase().includes('teléfono') || backendMsg.toLowerCase().includes('telefono')) {
        return { mensaje: backendMsg, campo: 'phone' };
      }
      return { mensaje: backendMsg || 'Algunos datos son inválidos. Revisa el formulario.' };
    }
    if (error.status >= 500) {
      return { mensaje: 'Error interno del servidor. Intenta de nuevo más tarde.' };
    }
    return { mensaje: backendMsg || 'Error al registrar usuario. Intenta de nuevo.' };
  }

  // ─────────────────────────────────────────────────────────────────
  // LOGOUT
  // ─────────────────────────────────────────────────────────────────

  logout(): void {
    this.eliminarToken();
    this.eliminarRefreshToken();
    localStorage.removeItem(this.USER_KEY);
  }

  // ─────────────────────────────────────────────────────────────────
  // IS AUTHENTICATED
  // ─────────────────────────────────────────────────────────────────

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirado = payload.exp * 1000 < Date.now();
      if (expirado) {
        this.eliminarToken();
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

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  guardarToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  eliminarToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  // ─────────────────────────────────────────────────────────────────
  // GESTIÓN DE REFRESH TOKEN
  // ─────────────────────────────────────────────────────────────────

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  guardarRefreshToken(refreshToken: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  eliminarRefreshToken(): void {
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No hay refresh token disponible'));
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        this.guardarToken(response.token);
        if (response.refreshToken) {
          this.guardarRefreshToken(response.refreshToken);
        }
      }),
      catchError(error => {
        this.logout();
        const mensaje = error.error?.mensaje || 'Sesión expirada, inicia sesión nuevamente';
        return throwError(() => new Error(mensaje));
      })
    );
  }

  // ─────────────────────────────────────────────────────────────────
  // UTILIDADES DE EXPIRACIÓN
  // ─────────────────────────────────────────────────────────────────

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
    return this.getUsuario()?.role ?? null;
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
   * FIX ALOJ-7: ahora incluye id y name para que el formulario de creación
   * pueda enviar el hostId correcto al backend.
   */
  private guardarSesion(response: AuthResponse): void {
    this.guardarToken(response.token);
    if (response.refreshToken) {
      this.guardarRefreshToken(response.refreshToken);
    }
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      id:    response.userId,  // ← FIX: necesario para hostId al crear alojamiento
      name:  response.name,    // ← FIX: necesario para mostrar nombre en navbar
      email: response.email,
      role:  response.rol
    }));
  }
}