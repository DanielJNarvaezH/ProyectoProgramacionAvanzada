package com.example.Alojamientos.RespuestaComentario;

import com.example.Alojamientos.businessLayer.dto.RespuestaComentarioDTO;
import com.example.Alojamientos.businessLayer.service.RespuestaComentarioService;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.RespuestaComentarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ComentarioRepository;
import com.example.Alojamientos.persistenceLayer.repository.RespuestaComentarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RespuestaComentarioServiceTest {

    @Mock
    private RespuestaComentarioRepository respuestaRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private RespuestaComentarioDataMapper mapper;

    @InjectMocks
    private RespuestaComentarioService service;

    private RespuestaComentarioEntity entity;
    private RespuestaComentarioDTO dto;
    private ComentarioEntity comentario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        comentario = ComentarioEntity.builder()
                .id(1)
                .texto("Buen servicio")
                .build();

        entity = RespuestaComentarioEntity.builder()
                .id(1)
                .comentario(comentario)
                .texto("Gracias por tu comentario!")
                .fechaCreacion(LocalDateTime.now())
                .build();

        dto = RespuestaComentarioDTO.builder()
                .commentId(1)
                .hostId(10)
                .text("Gracias por tu comentario!")
                .build();
    }

    // 1. Crear respuesta válida
    @Test
    void crearRespuesta_valida() {
        when(comentarioRepository.findById(1)).thenReturn(Optional.of(comentario));
        when(respuestaRepository.existsByComentario_Id(1)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(respuestaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.crearRespuesta(dto);
        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
    }

    // 2. Crear respuesta sin comentario (error)
    @Test
    void crearRespuesta_comentarioNoExisteDebeFallar() {
        when(comentarioRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.crearRespuesta(dto));
    }

    // 3. Crear respuesta ya existente (error)
    @Test
    void crearRespuesta_yaExisteDebeFallar() {
        when(comentarioRepository.findById(1)).thenReturn(Optional.of(comentario));
        when(respuestaRepository.existsByComentario_Id(1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.crearRespuesta(dto));
    }

    // 4. Crear respuesta con texto muy largo
    @Test
    void crearRespuesta_textoMuyLargoDebeFallar() {
        when(comentarioRepository.findById(1)).thenReturn(Optional.of(comentario));
        when(respuestaRepository.existsByComentario_Id(1)).thenReturn(false);
        dto.setText("x".repeat(600));
        assertThrows(IllegalArgumentException.class, () -> service.crearRespuesta(dto));
    }

    // 5. Listar respuestas por comentario con resultados
    @Test
    void listarPorComentario_conResultados() {
        when(respuestaRepository.findByComentario_Id(1)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.listarPorComentario(1);
        assertEquals(1, result.size());
        assertEquals(dto.getText(), result.get(0).getText());
    }

    // 6. Listar respuestas por comentario vacío
    @Test
    void listarPorComentario_vacio() {
        when(respuestaRepository.findByComentario_Id(1)).thenReturn(Collections.emptyList());
        var result = service.listarPorComentario(1);
        assertTrue(result.isEmpty());
    }

    // 7. Obtener respuesta por ID existente
    @Test
    void obtenerPorId_existente() {
        when(respuestaRepository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.obtenerPorId(1);
        assertEquals(dto.getText(), result.getText());
    }

    // 8. Obtener respuesta por ID inexistente
    @Test
    void obtenerPorId_inexistenteDebeFallar() {
        when(respuestaRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(99));
    }

    // 9. Actualizar respuesta existente
    @Test
    void actualizarRespuesta_existente() {
        when(respuestaRepository.findById(1)).thenReturn(Optional.of(entity));
        when(respuestaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        var result = service.actualizarRespuesta(1, "Nuevo texto");
        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
    }

    // 10. Actualizar respuesta inexistente
    @Test
    void actualizarRespuesta_inexistenteDebeFallar() {
        when(respuestaRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.actualizarRespuesta(99, "Hola"));
    }

    // 11. Actualizar respuesta con texto muy largo
    @Test
    void actualizarRespuesta_textoLargoDebeFallar() {
        when(respuestaRepository.findById(1)).thenReturn(Optional.of(entity));
        String textoLargo = "x".repeat(600);
        assertThrows(IllegalArgumentException.class, () -> service.actualizarRespuesta(1, textoLargo));
    }

    // 12. Eliminar respuesta existente
    @Test
    void eliminarRespuesta_existente() {
        when(respuestaRepository.existsById(1)).thenReturn(true);
        service.eliminarRespuesta(1);
        verify(respuestaRepository, times(1)).deleteById(1);
    }

    // 13. Eliminar respuesta inexistente
    @Test
    void eliminarRespuesta_inexistenteDebeFallar() {
        when(respuestaRepository.existsById(99)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.eliminarRespuesta(99));
    }

    // 14. Mapper: convertir DTO → Entity
    @Test
    void mapper_toEntity_valida() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        RespuestaComentarioEntity result = mapper.toEntity(dto);
        assertEquals(dto.getText(), result.getTexto());
    }

    // 15. Mapper: convertir Entity → DTO
    @Test
    void mapper_toDTO_valida() {
        when(mapper.toDTO(entity)).thenReturn(dto);
        RespuestaComentarioDTO result = mapper.toDTO(entity);
        assertEquals(entity.getTexto(), result.getText());
    }
}
