export interface RegisterRequest {
  name: string;
  email: string;
  phone: string;
  password: string;
  birthDate: string;
  role: 'USUARIO' | 'ANFITRION';
}
