import {
  Component, Input, OnChanges, SimpleChanges,
  AfterViewInit, OnDestroy, ElementRef, ViewChild, NgZone
} from '@angular/core';
import { Router } from '@angular/router';
import * as L from 'leaflet';
import { Alojamiento } from '../../../../models/alojamiento.model';

/**
 * MapaAlojamientosComponent — ALOJ-17
 *
 * Mapa interactivo con Leaflet que muestra marcadores
 * para cada alojamiento filtrado en el listado.
 *
 * Uso:
 *   <app-mapa-alojamientos [alojamientos]="alojamientosFiltrados"></app-mapa-alojamientos>
 */
@Component({
  selector: 'app-mapa-alojamientos',
  standalone: false,
  templateUrl: './mapa-alojamientos.html',
  styleUrls: ['./mapa-alojamientos.scss']
})
export class MapaAlojamientosComponent implements AfterViewInit, OnChanges, OnDestroy {

  @Input() alojamientos: Alojamiento[] = [];
  @ViewChild('mapaContainer') mapaContainer!: ElementRef;

  private mapa!: L.Map;
  private marcadores: L.Marker[] = [];
  private inicializado = false;

  constructor(private router: Router, private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    // Espera un tick para que el DOM esté listo
    setTimeout(() => {
      this.inicializarMapa();
      this.inicializado = true;
      this.actualizarMarcadores();
    }, 100);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['alojamientos'] && this.inicializado) {
      this.actualizarMarcadores();
    }
  }

  ngOnDestroy(): void {
    if (this.mapa) {
      this.mapa.remove();
    }
  }

  // ── Inicializar mapa ──────────────────────────────────────────

  private inicializarMapa(): void {
    // Centro por defecto: Colombia
    this.mapa = L.map(this.mapaContainer.nativeElement, {
      center: [4.5709, -74.2973],
      zoom: 6,
      zoomControl: true,
      attributionControl: true
    });

    // Tiles de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 18
    }).addTo(this.mapa);
  }

  // ── Marcadores ────────────────────────────────────────────────

  private actualizarMarcadores(): void {
    if (!this.mapa) return;

    // Limpiar marcadores anteriores
    this.marcadores.forEach(m => m.remove());
    this.marcadores = [];

    const conCoordenadas = this.alojamientos.filter(
      a => a.latitude && a.longitude
    );

    if (conCoordenadas.length === 0) return;

    // Icono personalizado con colores Hosped
    const iconoHosped = L.divIcon({
      className: '',
      html: `<div class="marcador-hosped">
               <div class="marcador-hosped__pin">
                 <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                   <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/>
                 </svg>
               </div>
             </div>`,
      iconSize: [32, 40],
      iconAnchor: [16, 40],
      popupAnchor: [0, -40]
    });

    conCoordenadas.forEach(aloj => {
      const marcador = L.marker([aloj.latitude, aloj.longitude], { icon: iconoHosped })
        .addTo(this.mapa);

      // Popup con info del alojamiento
      const precio = aloj.pricePerNight
        ? `$${aloj.pricePerNight.toLocaleString('es-CO')}/noche`
        : '';

      const imagen = aloj.mainImage
        ? `<img src="${aloj.mainImage}" alt="${aloj.name}" class="popup-img" />`
        : `<div class="popup-img-placeholder"><span>Sin imagen</span></div>`;

      marcador.bindPopup(`
        <div class="popup-alojamiento">
          ${imagen}
          <div class="popup-body">
            <h3 class="popup-nombre">${aloj.name}</h3>
            <p class="popup-ciudad">
              <span class="popup-icon">📍</span>${aloj.city}
            </p>
            ${precio ? `<p class="popup-precio">${precio}</p>` : ''}
            <button class="popup-btn" data-id="${aloj.id}">Ver detalle</button>
          </div>
        </div>
      `, { maxWidth: 220 });

      // Navegar al detalle al hacer clic en el botón del popup
      marcador.on('popupopen', () => {
        setTimeout(() => {
          const btn = document.querySelector(`.popup-btn[data-id="${aloj.id}"]`);
          if (btn) {
            btn.addEventListener('click', () => {
              this.ngZone.run(() => {
                this.router.navigate(['/alojamientos', aloj.id]);
              });
            });
          }
        }, 50);
      });

      this.marcadores.push(marcador);
    });

    // Ajustar vista para mostrar todos los marcadores
    if (this.marcadores.length === 1) {
      this.mapa.setView(
        [conCoordenadas[0].latitude, conCoordenadas[0].longitude],
        13
      );
    } else if (this.marcadores.length > 1) {
      const grupo = L.featureGroup(this.marcadores);
      this.mapa.fitBounds(grupo.getBounds(), { padding: [40, 40] });
    }
  }
}
