export interface AuthResponse {
  token: string;
  refreshToken: string;  // ← nuevo: token de refresco de larga duración
  email: string;
  rol: string;
  mensaje: string;
}