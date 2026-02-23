export interface User {
  id?: number;
  nombre: string;
  correo: string;
  telefono?: string;
  fechaNacimiento?: string;
  rol: 'USUARIO' | 'ANFITRION' | 'ADMIN';
  descripcion?: string;
  foto?: string;
  activo?: boolean;
}
