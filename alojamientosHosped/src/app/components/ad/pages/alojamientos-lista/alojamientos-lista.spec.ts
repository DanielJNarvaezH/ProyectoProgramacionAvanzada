import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError }            from 'rxjs';

import { AlojamientosListaPageComponent }  from './alojamientos-lista';
import { AlojamientoService }              from '../../../../../services/AlojamientoService';
import { AlojamientoServicioService }      from '../../../../../services/AlojamientoServicioService';
import { FiltroListaService }              from '../../../../../services/FiltroListaService';
import { Alojamiento }                     from '../../../../models';

/**
 * alojamientos-lista.spec.ts — RESERV-13 (paso 1: buscar)
 *
 * Prueba el listado de alojamientos: carga, filtros y ordenamiento.
 * Es el primer paso del flujo buscar → seleccionar → reservar.
 */
describe('AlojamientosListaPageComponent', () => {

  let component: AlojamientosListaPageComponent;
  let fixture:   ComponentFixture<AlojamientosListaPageComponent>;

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

  const alojamientoMock2: Alojamiento = {
    id:            2,
    hostId:        11,
    name:          'Apartamento Bogotá',
    description:   'Moderno apartamento en el centro',
    address:       'Calle 100 # 15-30',
    city:          'Bogotá',
    latitude:      4.7110,
    longitude:     -74.0721,
    pricePerNight: 80000,
    maxCapacity:   2,
    mainImage:     'https://imagen.com/apto.jpg',
    active:        true
  };

  const alojamientoServiceStub = {
    getAll:      jasmine.createSpy('getAll').and.returnValue(of([alojamientoMock, alojamientoMock2])),
    getCercanos: jasmine.createSpy('getCercanos').and.returnValue(of([alojamientoMock]))
  };

  const alojamientoServicioServiceStub = {
    getServiciosDisponibles:    jasmine.createSpy('getServiciosDisponibles').and.returnValue(of([])),
    getAlojamientosByServicio:  jasmine.createSpy('getAlojamientosByServicio').and.returnValue(of([]))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AlojamientosListaPageComponent],
      providers: [
        { provide: AlojamientoService,         useValue: alojamientoServiceStub },
        { provide: AlojamientoServicioService, useValue: alojamientoServicioServiceStub },
        FiltroListaService   // servicio real — mantiene estado entre tests dentro del bloque
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(AlojamientosListaPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    alojamientoServiceStub.getAll.calls.reset();
  });

  // ── Creación ──────────────────────────────────────────────────

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // ── Carga inicial ─────────────────────────────────────────────

  it('debería llamar a getAll() al iniciar', () => {
    expect(alojamientoServiceStub.getAll).toHaveBeenCalled();
  });

  it('debería cargar los alojamientos y mostrarlos en la lista', () => {
    expect(component.alojamientos.length).toBe(2);
    expect(component.alojamientos[0].name).toBe('Casa Campestre Armenia');
  });

  it('debería dejar cargando en false tras cargar los datos', () => {
    expect(component.cargando).toBeFalse();
  });

  it('debería mostrar error si el API falla al cargar', () => {
    alojamientoServiceStub.getAll.and.returnValue(
      throwError(() => new Error('Error de servidor'))
    );
    component.cargarAlojamientos();
    expect(component.error).toBeTruthy();
    expect(component.cargando).toBeFalse();
  });

  // ── Filtrado por texto ─────────────────────────────────────────

  it('filtrar() debería encontrar alojamientos que coincidan con el nombre', () => {
    component.alojamientos    = [alojamientoMock, alojamientoMock2];
    component.terminoBusqueda = 'armenia';
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(1);
    expect(component.alojamientosFiltrados[0].city).toBe('Armenia');
  });

  it('filtrar() debería encontrar alojamientos que coincidan con la ciudad', () => {
    component.alojamientos    = [alojamientoMock, alojamientoMock2];
    component.terminoBusqueda = 'bogotá';
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(1);
    expect(component.alojamientosFiltrados[0].city).toBe('Bogotá');
  });

  it('filtrar() debería retornar lista vacía si el término no coincide', () => {
    component.alojamientos    = [alojamientoMock, alojamientoMock2];
    component.terminoBusqueda = 'zzz_no_existe';
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(0);
  });

  // ── Filtrado por precio ────────────────────────────────────────

  it('filtrar() debería excluir alojamientos por encima del precio máximo', () => {
    component.alojamientos = [alojamientoMock, alojamientoMock2];
    component.precioMax    = 100000;
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(1);
    expect(component.alojamientosFiltrados[0].pricePerNight).toBeLessThanOrEqual(100000);
  });

  it('filtrar() debería excluir alojamientos por debajo del precio mínimo', () => {
    component.alojamientos = [alojamientoMock, alojamientoMock2];
    component.precioMin    = 120000;
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(1);
    expect(component.alojamientosFiltrados[0].pricePerNight).toBeGreaterThanOrEqual(120000);
  });

  // ── Filtrado por capacidad ─────────────────────────────────────

  it('filtrar() debería excluir alojamientos con capacidad menor a la mínima', () => {
    component.alojamientos = [alojamientoMock, alojamientoMock2];
    component.capacidadMin = 3;
    component.filtrar();
    expect(component.alojamientosFiltrados.length).toBe(1);
    expect(component.alojamientosFiltrados[0].maxCapacity).toBeGreaterThanOrEqual(3);
  });

  // ── Ordenamiento ──────────────────────────────────────────────

  it('filtrar() debería ordenar por precio ascendente', () => {
    component.alojamientos = [alojamientoMock, alojamientoMock2];
    component.ordenamiento = 'precio-asc';
    component.filtrar();
    expect(component.alojamientosFiltrados[0].pricePerNight)
      .toBeLessThanOrEqual(component.alojamientosFiltrados[1].pricePerNight);
  });

  it('filtrar() debería ordenar por precio descendente', () => {
    component.alojamientos = [alojamientoMock, alojamientoMock2];
    component.ordenamiento = 'precio-desc';
    component.filtrar();
    expect(component.alojamientosFiltrados[0].pricePerNight)
      .toBeGreaterThanOrEqual(component.alojamientosFiltrados[1].pricePerNight);
  });

  // ── Paginación ────────────────────────────────────────────────

  it('alojamientosPagina debería retornar solo los de la página actual', () => {
    component.alojamientosFiltrados = [alojamientoMock, alojamientoMock2];
    component.paginaActual          = 1;
    expect(component.alojamientosPagina.length).toBeLessThanOrEqual(component.ITEMS_POR_PAGINA);
  });

  it('totalMostrados debería reflejar el número de alojamientos filtrados', () => {
    component.alojamientosFiltrados = [alojamientoMock];
    expect(component.totalMostrados).toBe(1);
  });
});
