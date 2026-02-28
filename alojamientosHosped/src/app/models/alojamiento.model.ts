/**
 * Interfaz Alojamiento — alineada con AlojamientoDTO del backend.
 *
 * Campos en inglés porque el backend (AlojamientoDTO.java) los expone así:
 * hostId, name, description, address, city, latitude, longitude,
 * pricePerNight, maxCapacity, mainImage, active.
 */
export interface Alojamiento {
  id?: number;
  hostId: number;
  name: string;
  description: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  pricePerNight: number;
  maxCapacity: number;
  mainImage?: string;
  active?: boolean;
}