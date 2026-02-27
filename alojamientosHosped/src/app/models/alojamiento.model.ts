export interface Alojamiento {
  id?: number;
  idAnfitrion: number;
  nombre: string;
  descripcion: string;
  direccion: string;
  ciudad: string;
  latitud: number;
  longitud: number;
  precioPorNoche: number;
  capacidadMaxima: number;
  imagenPrincipal?: string;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}
