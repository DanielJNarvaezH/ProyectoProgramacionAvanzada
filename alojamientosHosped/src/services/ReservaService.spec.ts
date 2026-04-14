import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReservaService }                                 from './ReservaService';
import { Reserva, CrearReservaRequest }                   from '../app/models';
import { environment }                                    from '../environments/environment';

/**
 * ReservaService.spec.ts — RESERV-13
 *
 * Tests del servicio de reservas usando HttpTestingController.
 * Intercepta las peticiones HTTP sin llamar al backend real,
 * siguiendo el mismo patrón que AlojamientoService.spec.ts.
 *
 * Métodos probados:
 *   create()           → POST   /api/reservas
 *   getByUser()        → GET    /api/reservas/huesped/:guestId
 *   getByAlojamiento() → GET    /api/reservas/alojamiento/:lodgingId
 *   getById()          → GET    /api/reservas/:id
 *   cancel()           → DELETE /api/reservas/:id?motivo=...
 */
describe('ReservaService', () => {

  let service:  ReservaService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/reservas`;

  const reservaMock: Reserva = {
    id:         1,
    guestId:    5,
    lodgingId:  10,
    startDate:  '2025-12-01',
    endDate:    '2025-12-04',
    numGuests:  2,
    totalPrice: 450000,
    status:     'CONFIRMADA'
  };

  const crearReservaMock: CrearReservaRequest = {
    guestId:    5,
    lodgingId:  10,
    startDate:  '2025-12-01',
    endDate:    '2025-12-04',
    numGuests:  2,
    totalPrice: 450000,
    status:     'CONFIRMADA'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports:   [HttpClientTestingModule],
      providers: [ReservaService]
    });
    service  = TestBed.inject(ReservaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ── CREATE ────────────────────────────────────────────────────

  it('create() debe enviar POST y retornar la reserva creada', () => {
    service.create(crearReservaMock).subscribe(reserva => {
      expect(reserva.id).toBe(1);
      expect(reserva.status).toBe('CONFIRMADA');
      expect(reserva.totalPrice).toBe(450000);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(crearReservaMock);
    req.flush(reservaMock);
  });

  it('create() debe enviar guestId, lodgingId, fechas y huéspedes correctamente', () => {
    service.create(crearReservaMock).subscribe(reserva => {
      expect(reserva.guestId).toBe(5);
      expect(reserva.lodgingId).toBe(10);
      expect(reserva.startDate).toBe('2025-12-01');
      expect(reserva.endDate).toBe('2025-12-04');
      expect(reserva.numGuests).toBe(2);
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush(reservaMock);
  });

  it('create() debe manejar error 409 cuando hay conflicto de fechas', () => {
    service.create(crearReservaMock).subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush('El alojamiento no está disponible en las fechas seleccionadas',
      { status: 409, statusText: 'Conflict' });
  });

  it('create() debe manejar error 400 cuando los datos son inválidos', () => {
    service.create(crearReservaMock).subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush('La fecha de inicio no puede ser anterior a hoy',
      { status: 400, statusText: 'Bad Request' });
  });

  // ── GET BY USER ───────────────────────────────────────────────

  it('getByUser() debe retornar la lista de reservas del huésped', () => {
    service.getByUser(5).subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].guestId).toBe(5);
      expect(lista[0].status).toBe('CONFIRMADA');
    });

    const req = httpMock.expectOne(`${apiUrl}/huesped/5`);
    expect(req.request.method).toBe('GET');
    req.flush([reservaMock]);
  });

  it('getByUser() debe retornar lista vacía si el huésped no tiene reservas', () => {
    service.getByUser(99).subscribe(lista => {
      expect(lista.length).toBe(0);
    });

    const req = httpMock.expectOne(`${apiUrl}/huesped/99`);
    req.flush([]);
  });

  it('getByUser() debe manejar error 404 si no existen reservas para el huésped', () => {
    service.getByUser(999).subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(`${apiUrl}/huesped/999`);
    req.flush('No se encontraron reservas para el huésped',
      { status: 404, statusText: 'Not Found' });
  });

  // ── GET BY ALOJAMIENTO ────────────────────────────────────────

  it('getByAlojamiento() debe retornar las reservas del alojamiento', () => {
    service.getByAlojamiento(10).subscribe(lista => {
      expect(lista.length).toBe(1);
      expect(lista[0].lodgingId).toBe(10);
    });

    const req = httpMock.expectOne(`${apiUrl}/alojamiento/10`);
    expect(req.request.method).toBe('GET');
    req.flush([reservaMock]);
  });

  it('getByAlojamiento() debe manejar error 404 si no hay reservas para el alojamiento', () => {
    service.getByAlojamiento(999).subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(`${apiUrl}/alojamiento/999`);
    req.flush('No se encontraron reservas para el alojamiento',
      { status: 404, statusText: 'Not Found' });
  });

  // ── GET BY ID ─────────────────────────────────────────────────

  it('getById() debe retornar la reserva correcta por ID', () => {
    service.getById(1).subscribe(reserva => {
      expect(reserva.id).toBe(1);
      expect(reserva.startDate).toBe('2025-12-01');
      expect(reserva.endDate).toBe('2025-12-04');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(reservaMock);
  });

  it('getById() debe manejar error 404 si la reserva no existe', () => {
    service.getById(999).subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Reserva no encontrada con id: 999',
      { status: 404, statusText: 'Not Found' });
  });

  // ── CANCEL ────────────────────────────────────────────────────

  it('cancel() debe enviar DELETE con el motivo como query param', () => {
    service.cancel(1, 'Cambio de planes').subscribe(res => {
      expect(res).toBeNull();
    });

    const req = httpMock.expectOne(r =>
      r.url === `${apiUrl}/1` && r.params.get('motivo') === 'Cambio de planes'
    );
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('cancel() debe manejar error 400 cuando faltan menos de 48h para el check-in', () => {
    service.cancel(1, 'Emergencia').subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(r => r.url === `${apiUrl}/1`);
    req.flush('No se puede cancelar la reserva. La cancelación debe hacerse con al menos 48 horas de anticipación',
      { status: 400, statusText: 'Bad Request' });
  });

  it('cancel() debe manejar error 400 si la reserva ya fue cancelada', () => {
    service.cancel(1, 'Motivo válido').subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(r => r.url === `${apiUrl}/1`);
    req.flush('La reserva ya fue cancelada previamente',
      { status: 400, statusText: 'Bad Request' });
  });

  it('cancel() debe manejar error 404 si la reserva no existe', () => {
    service.cancel(999, 'Motivo válido').subscribe({
      next:  () => fail('Debería haber fallado'),
      error: (err) => { expect(err).toBeTruthy(); }
    });

    const req = httpMock.expectOne(r => r.url === `${apiUrl}/999`);
    req.flush('Reserva no encontrada con id: 999',
      { status: 404, statusText: 'Not Found' });
  });
});
