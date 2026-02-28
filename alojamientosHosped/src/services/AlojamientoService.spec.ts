import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AlojamientoService } from './AlojamientoService';
import { Alojamiento } from '../app/models/alojamiento.model';
import { environment } from '../environments/environment';

describe('AlojamientoService', () => {
  let service: AlojamientoService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/alojamientos`;

  // ── Dato de prueba reutilizable ───────────────────────────────
  const alojamientoMock: Alojamiento = {
    id: 1,
    idAnfitrion: 10,
    nombre: 'Casa en el campo',
    descripcion: 'Hermosa casa rodeada de naturaleza',
    direccion: 'Calle 123',
    ciudad: 'Armenia',
    latitud: 4.5339,
    longitud: -75.6811,
    precioPorNoche: 150000,
    capacidadMaxima: 4,
    imagenPrincipal: 'https://imagen.com/casa.jpg',
    activo: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AlojamientoService]
    });

    service = TestBed.inject(AlojamientoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verifica que no queden peticiones pendientes
  });

  // ─────────────────────────────────────────────────────────────
  // GET ALL
  // ─────────────────────────────────────────────────────────────

  it('getAll() debe retornar una lista de alojamientos', () => {
    const mockLista: Alojamiento[] = [alojamientoMock];

    service.getAll().subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].nombre).toBe('Casa en el campo');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockLista);
  });

  it('getAll() debe retornar lista vacía si no hay alojamientos', () => {
    service.getAll().subscribe(lista => {
      expect(lista.length).toBe(0);
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush([]);
  });

  // ─────────────────────────────────────────────────────────────
  // GET BY ID
  // ─────────────────────────────────────────────────────────────

  it('getById() debe retornar el alojamiento correcto', () => {
    service.getById(1).subscribe(alojamiento => {
      expect(alojamiento.id).toBe(1);
      expect(alojamiento.ciudad).toBe('Armenia');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(alojamientoMock);
  });

  it('getById() debe manejar error si el alojamiento no existe', () => {
    service.getById(999).subscribe({
      next: () => fail('Debería haber fallado'),
      error: (err) => {
        expect(err.message).toContain('Error al obtener el alojamiento con ID 999');
      }
    });

    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─────────────────────────────────────────────────────────────
  // CREATE
  // ─────────────────────────────────────────────────────────────

  it('create() debe crear un alojamiento y retornarlo', () => {
    const nuevoAlojamiento: Alojamiento = { ...alojamientoMock, id: undefined };

    service.create(nuevoAlojamiento).subscribe(creado => {
      expect(creado.id).toBe(1);
      expect(creado.nombre).toBe('Casa en el campo');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevoAlojamiento);
    req.flush(alojamientoMock);
  });

  it('create() debe manejar error del servidor', () => {
    const nuevoAlojamiento: Alojamiento = { ...alojamientoMock, id: undefined };

    service.create(nuevoAlojamiento).subscribe({
      next: () => fail('Debería haber fallado'),
      error: (err) => {
        expect(err.message).toContain('Error al crear el alojamiento');
      }
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });

  // ─────────────────────────────────────────────────────────────
  // UPDATE
  // ─────────────────────────────────────────────────────────────

  it('update() debe actualizar el alojamiento y retornarlo', () => {
    const actualizado: Alojamiento = { ...alojamientoMock, nombre: 'Casa renovada' };

    service.update(1, actualizado).subscribe(resultado => {
      expect(resultado.nombre).toBe('Casa renovada');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(actualizado);
    req.flush(actualizado);
  });

  it('update() debe manejar error si el alojamiento no existe', () => {
    service.update(999, alojamientoMock).subscribe({
      next: () => fail('Debería haber fallado'),
      error: (err) => {
        expect(err.message).toContain('Error al actualizar el alojamiento con ID 999');
      }
    });

    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // ─────────────────────────────────────────────────────────────
  // DELETE
  // ─────────────────────────────────────────────────────────────

  it('delete() debe eliminar el alojamiento correctamente', () => {
    service.delete(1).subscribe(resultado => {
      expect(resultado).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('delete() debe manejar error si tiene reservas activas', () => {
    service.delete(1).subscribe({
      next: () => fail('Debería haber fallado'),
      error: (err) => {
        expect(err.message).toContain('Error al eliminar el alojamiento con ID 1');
      }
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    req.flush('Tiene reservas activas', { status: 400, statusText: 'Bad Request' });
  });

  // ─────────────────────────────────────────────────────────────
  // GET BY ANFITRION
  // ─────────────────────────────────────────────────────────────

  it('getByAnfitrion() debe retornar alojamientos del anfitrión', () => {
    const mockLista: Alojamiento[] = [alojamientoMock];

    service.getByAnfitrion(10).subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].idAnfitrion).toBe(10);
    });

    const req = httpMock.expectOne(`${apiUrl}/anfitrion/10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLista);
  });

  // ─────────────────────────────────────────────────────────────
  // GET BY CIUDAD
  // ─────────────────────────────────────────────────────────────

  it('getByCiudad() debe retornar alojamientos de la ciudad', () => {
    const mockLista: Alojamiento[] = [alojamientoMock];

    service.getByCiudad('Armenia').subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].ciudad).toBe('Armenia');
    });

    const req = httpMock.expectOne(`${apiUrl}/ciudad/Armenia`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLista);
  });
});
