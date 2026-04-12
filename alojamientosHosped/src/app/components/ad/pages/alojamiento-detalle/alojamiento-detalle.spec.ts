import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule }    from '@angular/forms';
import { Router }         from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AlojamientoDetallePageComponent } from './alojamiento-detalle';
import { ReservaResumenModalComponent }    from '../../molecules/reserva-resumen-modal/reserva-resumen-modal';

import { AlojamientoService }         from '../../../../../services/AlojamientoService';
import { ReservaService }             from '../../../../../services/ReservaService';
import { AuthService }                from '../../../../../services/AuthService';
import { ImagenService }              from '../../../../../services/ImagenService';
import { ComentarioService }          from '../../../../../services/ComentarioService';
import { AlojamientoServicioService } from '../../../../../services/AlojamientoServicioService';
import { FavoritoService }            from '../../../../../services/FavoritoService';
import { MapService }                 from '../../../../../services/MapService';

import { Alojamiento, Reserva } from '../../../../models';

/**
 * alojamiento-detalle.spec.ts — RESERV-13 (pasos 2 y 3: seleccionar → reservar)
 *
 * Prueba el flujo dentro del detalle del alojamiento:
 *   - Carga del alojamiento seleccionado
 *   - Cálculo de noches y precio en tiempo real
 *   - Validación del formulario de reserva
 *   - Apertura del modal de resumen (RESERV-7) sin llamar al API
 *   - Confirmación → POST /api/reservas
 *   - Manejo de errores del API dentro del modal
 *   - Flujo integrado completo de los 4 pasos
 */
describe('AlojamientoDetallePageComponent', () => {

  let component: AlojamientoDetallePageComponent;
  let fixture:   ComponentFixture<AlojamientoDetallePageComponent>;

  const alojamientoMock: Alojamiento = {
    id:            1,
    hostId:        10,
    name:          'Casa Campestre Armenia',
    description:   'Hermosa casa rodeada de naturaleza',
    address:       'Vía El Caimo km 3',
    city:          'Armenia',
    latitude:      4.5339,
    longitude:     -75.6811,
    pricePerNight: 150000,
    maxCapacity:   4,
    mainImage:     'https://imagen.com/casa.jpg',
    active:        true
  };

  const reservaMock: Reserva = {
    id:         100,
    guestId:    5,
    lodgingId:  1,
    startDate:  '2025-12-01',
    endDate:    '2025-12-04',
    numGuests:  2,
    totalPrice: 450000,
    status:     'CONFIRMADA'
  };

  const usuarioMock = { id: 5, email: 'huesped@test.com', role: 'USUARIO', name: 'Huésped Test' };

  const alojamientoServiceStub = {
    getById: jasmine.createSpy('getById').and.returnValue(of(alojamientoMock)),
    delete:  jasmine.createSpy('delete').and.returnValue(of(null))
  };

  const reservaServiceStub = {
    create: jasmine.createSpy('create').and.returnValue(of(reservaMock))
  };

  const authServiceStub = {
    getUsuario: jasmine.createSpy('getUsuario').and.returnValue(usuarioMock)
  };

  const imagenServiceStub = {
    getByAlojamiento: jasmine.createSpy('getByAlojamiento').and.returnValue(of([]))
  };

  const comentarioServiceStub = {
    getByAlojamiento: jasmine.createSpy('getByAlojamiento').and.returnValue(of([])),
    getPromedio:      jasmine.createSpy('getPromedio').and.returnValue(of(0))
  };

  const alojamientoServicioServiceStub = {
    getServiciosByAlojamiento: jasmine.createSpy('getServiciosByAlojamiento').and.returnValue(of([]))
  };

  const favoritoServiceStub = {
    esFavorito: jasmine.createSpy('esFavorito').and.returnValue(of(false)),
    agregar:    jasmine.createSpy('agregar').and.returnValue(of(null)),
    eliminar:   jasmine.createSpy('eliminar').and.returnValue(of(null))
  };

  const mapServiceStub = {
    buildEmbedUrl: jasmine.createSpy('buildEmbedUrl').and.returnValue('')
  };

  const routerStub = { navigate: jasmine.createSpy('navigate') };

  const activatedRouteStub = {
    paramMap: of({ get: (_: string) => '1' }),
    snapshot: { queryParamMap: { get: (_: string) => null } }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        AlojamientoDetallePageComponent,
        ReservaResumenModalComponent
      ],
      imports: [FormsModule],
      providers: [
        { provide: AlojamientoService,         useValue: alojamientoServiceStub },
        { provide: ReservaService,             useValue: reservaServiceStub },
        { provide: AuthService,                useValue: authServiceStub },
        { provide: ImagenService,              useValue: imagenServiceStub },
        { provide: ComentarioService,          useValue: comentarioServiceStub },
        { provide: AlojamientoServicioService, useValue: alojamientoServicioServiceStub },
        { provide: FavoritoService,            useValue: favoritoServiceStub },
        { provide: MapService,                 useValue: mapServiceStub },
        { provide: Router,                     useValue: routerStub },
        { provide: ActivatedRoute,             useValue: activatedRouteStub }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(AlojamientoDetallePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    reservaServiceStub.create.calls.reset();
    routerStub.navigate.calls.reset();
    authServiceStub.getUsuario.and.returnValue(usuarioMock);
  });

  // ── Creación ──────────────────────────────────────────────────

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // ── Carga del alojamiento seleccionado ────────────────────────

  it('debería cargar el alojamiento al iniciar', () => {
    expect(alojamientoServiceStub.getById).toHaveBeenCalledWith(1);
    expect(component.alojamiento).toEqual(alojamientoMock);
    expect(component.cargando).toBeFalse();
  });

  it('debería mostrar error si el alojamiento no existe', () => {
    alojamientoServiceStub.getById.and.returnValue(
      throwError(() => new Error('Alojamiento no encontrado'))
    );
    component.cargarDetalle(999);
    expect(component.error).toBe('Alojamiento no encontrado');
    expect(component.cargando).toBeFalse();
  });

  // ── Cálculo de noches y precio ────────────────────────────────

  it('debería retornar 0 noches si no hay rango seleccionado', () => {
    component.rangoReserva = null;
    expect(component.noches).toBe(0);
  });

  it('debería retornar 0 en precioTotal si no hay fechas seleccionadas', () => {
    component.rangoReserva = null;
    expect(component.precioTotal).toBe(0);
  });

  it('debería calcular correctamente las noches entre dos fechas', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
    expect(component.noches).toBe(3);
  });

  it('debería calcular el precio total como noches × precio por noche', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
    expect(component.precioTotal).toBe(3 * alojamientoMock.pricePerNight);
  });

  it('debería actualizar el precio al cambiar el rango de fechas', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-06' };
    expect(component.noches).toBe(5);
    expect(component.precioTotal).toBe(5 * alojamientoMock.pricePerNight);
  });

  // ── Validación del formulario ─────────────────────────────────

  it('formularioValido debe ser false sin fechas seleccionadas', () => {
    component.rangoReserva = null;
    component.numGuests    = 1;
    expect(component.formularioValido).toBeFalse();
  });

  it('formularioValido debe ser false si los huéspedes superan la capacidad máxima', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
    component.numGuests    = alojamientoMock.maxCapacity + 1;
    expect(component.formularioValido).toBeFalse();
  });

  it('formularioValido debe ser false si numGuests es 0', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
    component.numGuests    = 0;
    expect(component.formularioValido).toBeFalse();
  });

  it('formularioValido debe ser true con fechas y huéspedes válidos', () => {
    component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
    component.numGuests    = 2;
    expect(component.formularioValido).toBeTrue();
  });

  // ── Modal de resumen — RESERV-7 ───────────────────────────────

  it('solicitarReserva() debe abrir el modal sin llamar al API', () => {
    component.rangoReserva        = { startDate: '2025-12-01', endDate: '2025-12-04' };
    component.numGuests           = 2;
    component.mostrarModalReserva = false;

    component.solicitarReserva();

    expect(component.mostrarModalReserva).toBeTrue();
    expect(reservaServiceStub.create).not.toHaveBeenCalled();
  });

  it('solicitarReserva() no debe abrir el modal si el formulario es inválido', () => {
    component.rangoReserva = null;
    component.solicitarReserva();
    expect(component.mostrarModalReserva).toBeFalse();
  });

  it('cerrarModalReserva() debe cerrar el modal y limpiar el error', () => {
    component.mostrarModalReserva = true;
    component.errorReserva        = 'Error previo';
    component.enviandoReserva     = false;

    component.cerrarModalReserva();

    expect(component.mostrarModalReserva).toBeFalse();
    expect(component.errorReserva).toBe('');
  });

  it('cerrarModalReserva() no debe cerrar si la petición está en curso', () => {
    component.mostrarModalReserva = true;
    component.enviandoReserva     = true;

    component.cerrarModalReserva();

    expect(component.mostrarModalReserva).toBeTrue();
  });

  // ── Confirmación → POST /api/reservas ─────────────────────────

  it('confirmarReserva() debe llamar a ReservaService.create con los datos correctos',
    fakeAsync(() => {
      component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
      component.numGuests    = 2;

      component.confirmarReserva();
      tick();

      expect(reservaServiceStub.create).toHaveBeenCalledWith({
        guestId:    usuarioMock.id,
        lodgingId:  alojamientoMock.id!,
        startDate:  '2025-12-01',
        endDate:    '2025-12-04',
        numGuests:  2,
        totalPrice: 3 * alojamientoMock.pricePerNight,
        status:     'CONFIRMADA'
      });
    })
  );

  it('confirmarReserva() debe cerrar el modal y activar reservaExitosa al tener éxito',
    fakeAsync(() => {
      reservaServiceStub.create.and.returnValue(of(reservaMock));
      component.rangoReserva        = { startDate: '2025-12-01', endDate: '2025-12-04' };
      component.numGuests           = 2;
      component.mostrarModalReserva = true;

      component.confirmarReserva();
      tick();

      expect(component.mostrarModalReserva).toBeFalse();
      expect(component.reservaExitosa).toBeTrue();
      expect(component.rangoReserva).toBeNull();
      expect(component.numGuests).toBe(1);
      expect(component.enviandoReserva).toBeFalse();
    })
  );

  it('confirmarReserva() debe mantener el modal abierto y mostrar el error si el API falla',
    fakeAsync(() => {
      reservaServiceStub.create.and.returnValue(
        throwError(() => new Error('El alojamiento no está disponible en esas fechas'))
      );
      component.rangoReserva        = { startDate: '2025-12-01', endDate: '2025-12-04' };
      component.numGuests           = 2;
      component.mostrarModalReserva = true;

      component.confirmarReserva();
      tick();

      expect(component.mostrarModalReserva).toBeTrue();
      expect(component.reservaExitosa).toBeFalsy();
      expect(component.errorReserva).toBe('El alojamiento no está disponible en esas fechas');
      expect(component.enviandoReserva).toBeFalse();
    })
  );

  it('confirmarReserva() no debe ejecutarse si el usuario no está autenticado',
    fakeAsync(() => {
      authServiceStub.getUsuario.and.returnValue(null);
      component.rangoReserva = { startDate: '2025-12-01', endDate: '2025-12-04' };
      component.numGuests    = 2;

      component.confirmarReserva();
      tick();

      expect(reservaServiceStub.create).not.toHaveBeenCalled();
    })
  );

  it('confirmarReserva() no debe ejecutarse si no hay fechas seleccionadas',
    fakeAsync(() => {
      component.rangoReserva = null;

      component.confirmarReserva();
      tick();

      expect(reservaServiceStub.create).not.toHaveBeenCalled();
    })
  );

  // ── Flujo integrado completo ───────────────────────────────────

  it('flujo completo: seleccionar fechas → abrir modal → confirmar reserva',
    fakeAsync(() => {
      reservaServiceStub.create.and.returnValue(of(reservaMock));

      // Paso 1: usuario selecciona fechas en el calendario
      component.onRangoSeleccionado({ startDate: '2025-12-01', endDate: '2025-12-04' });
      expect(component.rangoReserva).toBeTruthy();
      expect(component.noches).toBe(3);
      expect(component.precioTotal).toBe(450000);

      // Paso 2: usuario ajusta número de huéspedes
      component.numGuests = 2;
      expect(component.formularioValido).toBeTrue();

      // Paso 3: clic en "Reservar" → se abre el modal, API NO se llama aún
      component.solicitarReserva();
      expect(component.mostrarModalReserva).toBeTrue();
      expect(reservaServiceStub.create).not.toHaveBeenCalled();

      // Paso 4: usuario confirma en el modal → se llama al API
      component.confirmarReserva();
      tick();

      expect(reservaServiceStub.create).toHaveBeenCalledTimes(1);
      expect(component.mostrarModalReserva).toBeFalse();
      expect(component.reservaExitosa).toBeTrue();
      expect(component.rangoReserva).toBeNull();
      expect(component.numGuests).toBe(1);
    })
  );
});
