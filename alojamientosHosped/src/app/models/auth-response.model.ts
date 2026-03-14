export interface AuthResponse {
  token: string;
  email: string;
  rol: string;
  mensaje: string;
  refreshToken?: string;
  userId?: number;  // ← ALOJ-7: necesario para enviar hostId al crear alojamiento
  name?: string;    // ← ALOJ-7: necesario para mostrar nombre en navbar
}