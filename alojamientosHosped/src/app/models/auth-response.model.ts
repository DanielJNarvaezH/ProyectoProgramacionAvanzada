export interface AuthResponse {
  token: string;
  email: string;
  rol: string;
  mensaje: string;
  refreshToken?: string;
}
