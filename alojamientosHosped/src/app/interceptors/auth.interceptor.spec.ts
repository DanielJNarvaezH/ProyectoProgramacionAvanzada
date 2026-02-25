import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {
  HTTP_INTERCEPTORS,
  HttpClient
} from '@angular/common/http';

import { AuthInterceptor } from './auth.interceptor';
import { AuthService } from '../../services/AuthService';

// ── Stub del AuthService ───────────────────────────────────────────
const authServiceStub = {
  getToken: jasmine.createSpy('getToken')
};

const TEST_URL = 'http://localhost:8080/alojamientos/api/test';

describe('AuthInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: AuthInterceptor,
          multi: true
        }
      ]
    });

    http     = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    authServiceStub.getToken.calls.reset();
  });

  // ── Con token ──────────────────────────────────────────────────────

  it('debería agregar el header Authorization cuando hay token', () => {
    authServiceStub.getToken.and.returnValue('mi.jwt.token');

    http.get(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe('Bearer mi.jwt.token');
    req.flush({});
  });

  it('el header debe tener el formato "Bearer <token>"', () => {
    authServiceStub.getToken.and.returnValue('eyJhbGciOiJIUzI1NiJ9.payload.signature');

    http.get(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.headers.get('Authorization'))
      .toMatch(/^Bearer eyJhbGciOiJIUzI1NiJ9\..+/);
    req.flush({});
  });

  it('debería preservar los demás headers existentes', () => {
    authServiceStub.getToken.and.returnValue('token123');

    http.get(TEST_URL, {
      headers: { 'Content-Type': 'application/json' }
    }).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.headers.get('Authorization')).toBe('Bearer token123');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    req.flush({});
  });

  it('debería funcionar con peticiones POST', () => {
    authServiceStub.getToken.and.returnValue('token-post');

    http.post(TEST_URL, { dato: 'valor' }).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-post');
    req.flush({});
  });

  it('debería funcionar con peticiones PUT', () => {
    authServiceStub.getToken.and.returnValue('token-put');

    http.put(TEST_URL, {}).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-put');
    req.flush({});
  });

  it('debería funcionar con peticiones DELETE', () => {
    authServiceStub.getToken.and.returnValue('token-delete');

    http.delete(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-delete');
    req.flush({});
  });

  // ── Sin token ──────────────────────────────────────────────────────

  it('no debería agregar Authorization si no hay token (null)', () => {
    authServiceStub.getToken.and.returnValue(null);

    http.get(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('no debería modificar la petición si el token es null', () => {
    authServiceStub.getToken.and.returnValue(null);

    http.get(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.url).toBe(TEST_URL);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  // ── Inmutabilidad ──────────────────────────────────────────────────

  it('no debería mutar la petición original (usa clone)', () => {
    authServiceStub.getToken.and.returnValue('token-clone');

    http.get(TEST_URL).subscribe();

    const req = httpMock.expectOne(TEST_URL);
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-clone');
    req.flush({});
  });
});
