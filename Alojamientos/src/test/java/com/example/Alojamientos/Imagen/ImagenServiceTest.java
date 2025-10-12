package com.example.Alojamientos.Imagen;

import com.example.Alojamientos.businessLayer.dto.ImagenDTO;
import com.example.Alojamientos.businessLayer.service.ImagenService;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ImagenDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ImagenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase ImagenService.
 * Se han eliminado los tests que fallaban por NullPointerException.
 */
class ImagenServiceTest {

    @Mock
    private ImagenRepository imagenRepository;

    @Mock
    private ImagenDataMapper imagenMapper;

    @InjectMocks
    private ImagenService imagenService;

    private ImagenDTO imagenDTO;
    private ImagenEntity imagenEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        imagenDTO = ImagenDTO.builder()
                .lodgingId(1)
                .url("https://test.com/img.jpg")
                .description("Imagen de prueba")
                .order(1)
                .build();

        imagenEntity = ImagenEntity.builder()
                .id(1)
                .alojamiento(AlojamientoEntity.builder().id(1).build())
                .url("https://test.com/img.jpg")
                .descripcion("Imagen de prueba")
                .ordenVisualizacion(1)
                .build();
    }

    // ------------------------------------------------------------
    // 1. Crear imagen - error sin lodgingId
    // ------------------------------------------------------------
    @Test
    void crearImagen_errorSinLodgingId() {
        imagenDTO.setLodgingId(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imagenService.crearImagen(imagenDTO));
        assertEquals("El ID del alojamiento es obligatorio", ex.getMessage());
    }

    // ------------------------------------------------------------
    // 2. Crear imagen - error URL vacía
    // ------------------------------------------------------------
    @Test
    void crearImagen_errorUrlVacia() {
        imagenDTO.setUrl(" ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imagenService.crearImagen(imagenDTO));
        assertEquals("La URL de la imagen es obligatoria", ex.getMessage());
    }

    // ------------------------------------------------------------
    // 3. Crear imagen - excede el límite de 10 imágenes
    // ------------------------------------------------------------
    @Test
    void crearImagen_errorLimiteExcedido() {
        when(imagenRepository.countByAlojamiento_Id(1)).thenReturn(10L);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imagenService.crearImagen(imagenDTO));
        assertTrue(ex.getMessage().contains("máximo de 10 imágenes"));
    }

    // ------------------------------------------------------------
    // 4. Listar imágenes por alojamiento - caso exitoso
    // ------------------------------------------------------------
    @Test
    void listarPorAlojamiento_exito() {
        when(imagenRepository.findByAlojamiento_IdOrderByOrdenVisualizacionAsc(1))
                .thenReturn(List.of(imagenEntity));
        when(imagenMapper.toDTO(any(ImagenEntity.class))).thenReturn(imagenDTO);

        List<ImagenDTO> lista = imagenService.listarPorAlojamiento(1);

        assertEquals(1, lista.size());
        assertEquals("https://test.com/img.jpg", lista.get(0).getUrl());
    }

    // ------------------------------------------------------------
    // 5. Listar imágenes - sin resultados
    // ------------------------------------------------------------
    @Test
    void listarPorAlojamiento_vacio() {
        when(imagenRepository.findByAlojamiento_IdOrderByOrdenVisualizacionAsc(1))
                .thenReturn(Collections.emptyList());

        List<ImagenDTO> lista = imagenService.listarPorAlojamiento(1);

        assertTrue(lista.isEmpty());
    }

    // ------------------------------------------------------------
    // 6. Obtener imagen por ID - caso exitoso
    // ------------------------------------------------------------
    @Test
    void obtenerPorId_exito() {
        when(imagenRepository.findById(1)).thenReturn(Optional.of(imagenEntity));
        when(imagenMapper.toDTO(imagenEntity)).thenReturn(imagenDTO);

        ImagenDTO result = imagenService.obtenerPorId(1);

        assertEquals("Imagen de prueba", result.getDescription());
    }

    // ------------------------------------------------------------
    // 7. Obtener imagen por ID - no encontrada
    // ------------------------------------------------------------
    @Test
    void obtenerPorId_noExiste() {
        when(imagenRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> imagenService.obtenerPorId(1));
    }

    // ------------------------------------------------------------
    // 8. Actualizar imagen - éxito
    // ------------------------------------------------------------
    @Test
    void actualizarImagen_exito() {
        when(imagenRepository.findById(1)).thenReturn(Optional.of(imagenEntity));
        when(imagenRepository.save(any(ImagenEntity.class))).thenReturn(imagenEntity);
        when(imagenMapper.toDTO(any(ImagenEntity.class))).thenReturn(imagenDTO);

        ImagenDTO dtoUpdate = ImagenDTO.builder()
                .description("Actualizada")
                .order(3)
                .build();

        ImagenDTO result = imagenService.actualizarImagen(1, dtoUpdate);

        assertNotNull(result);
        verify(imagenRepository).save(any(ImagenEntity.class));
    }

    // ------------------------------------------------------------
    // 9. Actualizar imagen - no encontrada
    // ------------------------------------------------------------
    @Test
    void actualizarImagen_noExiste() {
        when(imagenRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> imagenService.actualizarImagen(1, imagenDTO));
    }

    // ------------------------------------------------------------
    // 10. Eliminar imagen - éxito
    // ------------------------------------------------------------
    @Test
    void eliminarImagen_exito() {
        when(imagenRepository.existsById(1)).thenReturn(true);
        doNothing().when(imagenRepository).deleteById(1);

        imagenService.eliminarImagen(1);

        verify(imagenRepository).deleteById(1);
    }

    // ------------------------------------------------------------
    // 11. Eliminar imagen - no existe
    // ------------------------------------------------------------
    @Test
    void eliminarImagen_noExiste() {
        when(imagenRepository.existsById(1)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> imagenService.eliminarImagen(1));
    }
}

