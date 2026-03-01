export interface User {
  id?: number;
  name: string;
  email: string;
  phone?: string;
  birthDate?: string;
  role: 'USUARIO' | 'ANFITRION' | 'ADMIN';
  password?: string;
}
