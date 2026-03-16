import {
  Component, Input, Output, EventEmitter, OnInit,
  OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';

export interface ImagenSubida {
  id: string;           // public_id de Cloudinary o id de BD (prefijo 'bd_')
  bdId?: number;        // id real en la BD si ya estaba guardada
  url: string;
  orden: number;
  nombre: string;
  tamanio: number;
  subiendo: boolean;
  progreso: number;
  error?: string;
  esExistente?: boolean; // true si vino de BD, no de Cloudinary en esta sesión
}

export interface ImagenExistente {
  id: number;
  url: string;
  orden: number;
  nombre?: string;
}

@Component({
  standalone: false,
  selector: 'app-image-uploader',
  templateUrl: './image-uploader.html',
  styleUrls: ['./image-uploader.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImageUploaderComponent implements OnInit, OnDestroy {

  /** ID del alojamiento (para metadatos en Cloudinary) */
  @Input() alojamientoId?: number;

  /** Máximo de imágenes permitidas */
  @Input() maxImagenes = 8;

  /**
   * ALOJ-11: imágenes ya guardadas en BD para precargar al editar.
   * Formato: [{ id, url, orden, nombre? }]
   */
  @Input() imagenesExistentes: ImagenExistente[] = [];

  /** Emite la lista actualizada cada vez que cambia */
  @Output() imagenesChange = new EventEmitter<ImagenSubida[]>();

  /** Emite la URL de la imagen principal (orden = 0) */
  @Output() imagenPrincipalChange = new EventEmitter<string>();

  imagenes: ImagenSubida[] = [];
  isDragOver = false;
  private dragSrcIndex: number | null = null;

  private readonly CLOUD_NAME    = 'dxikq6rqs';
  private readonly UPLOAD_PRESET = 'hosped_unsigned';
  private readonly CLOUDINARY_URL = `https://api.cloudinary.com/v1_1/${this.CLOUD_NAME}/image/upload`;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // ALOJ-11: precargar imágenes existentes al editar
    if (this.imagenesExistentes && this.imagenesExistentes.length > 0) {
      this.imagenes = this.imagenesExistentes.map((img, i) => ({
        id:          `bd_${img.id}`,
        bdId:        img.id,
        url:         img.url,
        orden:       img.orden ?? i,
        nombre:      img.nombre ?? `imagen_${i + 1}`,
        tamanio:     0,
        subiendo:    false,
        progreso:    100,
        esExistente: true
      }));
      this.emitirCambio();
      this.cdr.markForCheck();
    }
  }

  // ── Drag & Drop zona ──────────────────────────────────────────────

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
    this.cdr.markForCheck();
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    this.cdr.markForCheck();
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    const files = event.dataTransfer?.files;
    if (files) this.procesarArchivos(Array.from(files));
    this.cdr.markForCheck();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.procesarArchivos(Array.from(input.files));
      input.value = '';
    }
  }

  // ── Procesamiento de archivos ──────────────────────────────────────

  private procesarArchivos(files: File[]): void {
    const imagenesValidas = files.filter(f => f.type.startsWith('image/'));
    const disponibles     = this.maxImagenes - this.imagenes.length;
    const aSubir          = imagenesValidas.slice(0, disponibles);
    aSubir.forEach(file => this.subirImagen(file));
  }

  private subirImagen(file: File): void {
    const tempId = `temp_${Date.now()}_${Math.random()}`;
    const nuevaImagen: ImagenSubida = {
      id:       tempId,
      url:      '',
      orden:    this.imagenes.length,
      nombre:   file.name,
      tamanio:  file.size,
      subiendo: true,
      progreso: 0
    };

    const reader = new FileReader();
    reader.onload = (e) => {
      nuevaImagen.url = e.target?.result as string;
      this.cdr.markForCheck();
    };
    reader.readAsDataURL(file);

    this.imagenes = [...this.imagenes, nuevaImagen];
    this.emitirCambio();

    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.UPLOAD_PRESET);
    formData.append('folder', `hosped/alojamientos${this.alojamientoId ? '/' + this.alojamientoId : ''}`);

    this.http.post(this.CLOUDINARY_URL, formData, {
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          const progreso = Math.round(100 * event.loaded / event.total);
          this.actualizarImagen(tempId, { progreso });
        } else if (event.type === HttpEventType.Response) {
          const body = event.body;
          this.actualizarImagen(tempId, {
            id:       body.public_id,
            url:      body.secure_url,
            subiendo: false,
            progreso: 100
          });
          this.emitirCambio();
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.actualizarImagen(tempId, {
          subiendo: false,
          error:    'Error al subir la imagen. Intenta de nuevo.'
        });
        this.cdr.markForCheck();
      }
    });
  }

  private actualizarImagen(id: string, cambios: Partial<ImagenSubida>): void {
    this.imagenes = this.imagenes.map(img =>
      img.id === id ? { ...img, ...cambios } : img
    );
  }

  // ── Eliminar ───────────────────────────────────────────────────────

  eliminar(index: number): void {
    this.imagenes = this.imagenes
      .filter((_, i) => i !== index)
      .map((img, i) => ({ ...img, orden: i }));
    this.emitirCambio();
    this.cdr.markForCheck();
  }

  reintentar(index: number): void {
    const img = this.imagenes[index];
    if (img?.error) this.eliminar(index);
  }

  // ── Reordenar ──────────────────────────────────────────────────────

  onCardDragStart(index: number): void {
    this.dragSrcIndex = index;
  }

  onCardDragOver(event: DragEvent, index: number): void {
    event.preventDefault();
  }

  onCardDrop(event: DragEvent, targetIndex: number): void {
    event.preventDefault();
    if (this.dragSrcIndex === null || this.dragSrcIndex === targetIndex) return;

    const reordenadas = [...this.imagenes];
    const [movida] = reordenadas.splice(this.dragSrcIndex, 1);
    reordenadas.splice(targetIndex, 0, movida);
    this.imagenes     = reordenadas.map((img, i) => ({ ...img, orden: i }));
    this.dragSrcIndex = null;
    this.emitirCambio();
    this.cdr.markForCheck();
  }

  moverIzquierda(index: number): void {
    if (index === 0) return;
    this.intercambiar(index, index - 1);
  }

  moverDerecha(index: number): void {
    if (index === this.imagenes.length - 1) return;
    this.intercambiar(index, index + 1);
  }

  private intercambiar(a: number, b: number): void {
    const arr = [...this.imagenes];
    [arr[a], arr[b]] = [arr[b], arr[a]];
    this.imagenes = arr.map((img, i) => ({ ...img, orden: i }));
    this.emitirCambio();
    this.cdr.markForCheck();
  }

  // ── Utilidades ─────────────────────────────────────────────────────

  get puedeAgregarMas(): boolean {
    return this.imagenes.length < this.maxImagenes;
  }

  get imagenPrincipal(): ImagenSubida | null {
    return this.imagenes.find(i => i.orden === 0) ?? null;
  }

  formatearTamanio(bytes: number): string {
    if (bytes === 0) return '';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  trackByIndex(index: number): number {
    return index;
  }

  private emitirCambio(): void {
    this.imagenesChange.emit([...this.imagenes]);
    const principal = this.imagenes.find(i => i.orden === 0 && !i.subiendo && !i.error);
    if (principal) this.imagenPrincipalChange.emit(principal.url);
  }

  ngOnDestroy(): void {}
}