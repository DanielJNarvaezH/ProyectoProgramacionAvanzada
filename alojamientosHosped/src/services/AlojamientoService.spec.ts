import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AlojamientoService } from './AlojamientoService';
import { Alojamiento } from '../app/models/alojamiento.model';
import { environment } from '../environments/environment';

describe('AlojamientoService', () => {
  let service: AlojamientoService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/alojamientos`;

  const alojamientoMock: Alojamiento = {
    id: 1,
    hostId: 10,
    name: 'Casa en el campo',
    description: 'Hermosa casa rodeada de naturaleza',
    address: 'Calle 123',
    city: 'Armenia',
    latitude: 4.5339,
    longitude: -75.6811,
    pricePerNight: 150000,
    maxCapacity: 4,
    mainImage: 'https://imagen.com/casa.jpg',
    active: true
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
    httpMock.verify();
  });

  // GET ALL
  it('getAll() debe retornar una lista de alojamientos', () => {
    service.getAll().subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].name).toBe('Casa en el campo');
    });
    const req = httpMock.expectOne(`${apiUrl}/activos`);
    expect(req.request.method).toBe('GET');
    req.flush([alojamientoMock]);
  });

  it('getAll() debe retornar lista vacia si no hay alojamientos', () => {
    service.getAll().subscribe(lista => {
      expect(lista.length).toBe(0);
    });
    const req = httpMock.expectOne(`${apiUrl}/activos`);
    req.flush([]);
  });

  // GET BY ID
  it('getById() debe retornar el alojamiento correcto', () => {
    service.getById(1).subscribe(alojamiento => {
      expect(alojamiento.id).toBe(1);
      expect(alojamiento.city).toBe('Armenia');
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(alojamientoMock);
  });

  it('getById() debe manejar error si el alojamiento no existe', () => {
    service.getById(999).subscribe({
      next: () => fail('Deberia haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });
    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // CREATE
  it('create() debe crear un alojamiento y retornarlo', () => {
    const nuevo: Alojamiento = { ...alojamientoMock, id: undefined };
    service.create(nuevo).subscribe(creado => {
      expect(creado.id).toBe(1);
      expect(creado.name).toBe('Casa en el campo');
    });
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(alojamientoMock);
  });

  it('create() debe manejar error del servidor', () => {
    const nuevo: Alojamiento = { ...alojamientoMock, id: undefined };
    service.create(nuevo).subscribe({
      next: () => fail('Deberia haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });
    const req = httpMock.expectOne(apiUrl);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });

  // UPDATE
  it('update() debe actualizar el alojamiento y retornarlo', () => {
    const actualizado: Alojamiento = { ...alojamientoMock, name: 'Casa renovada' };
    service.update(1, actualizado).subscribe(resultado => {
      expect(resultado.name).toBe('Casa renovada');
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(actualizado);
  });

  it('update() debe manejar error si el alojamiento no existe', () => {
    service.update(999, alojamientoMock).subscribe({
      next: () => fail('Deberia haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });
    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });
  });

  // DELETE
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
      next: () => fail('Deberia haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });
    const req = httpMock.expectOne(`${apiUrl}/1`);
    req.flush('Tiene reservas activas', { status: 400, statusText: 'Bad Request' });
  });

  // GET BY ANFITRION
  it('getByAnfitrion() debe retornar alojamientos del anfitrion', () => {
    service.getByAnfitrion(10).subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].hostId).toBe(10);
    });
    const req = httpMock.expectOne(`${apiUrl}/anfitrion/10`);
    expect(req.request.method).toBe('GET');
    req.flush([alojamientoMock]);
  });

  // GET BY CIUDAD
  it('getByCiudad() debe retornar alojamientos de la ciudad', () => {
    service.getByCiudad('Armenia').subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].city).toBe('Armenia');
    });
    const req = httpMock.expectOne(`${apiUrl}/buscar?ciudad=Armenia`);
    expect(req.request.method).toBe('GET');
    req.flush([alojamientoMock]);
  });
});
