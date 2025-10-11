package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.FavoritoDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.FavoritoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.FavoritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private FavoritoDataMapper favoritoMapper;

    @InjectMocks
    private FavoritoService favoritoService;

    private FavoritoEntity favoritoEntity;
    private FavoritoDTO favoritoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UsuarioEntity usuario = UsuarioEntity.builder().id(1).build();
        AlojamientoEntity alojamiento = AlojamientoEntity.builder().id(10).build();

        favoritoEntity = FavoritoEntity.builder()
                .id(100)
                .usuario(usuario)
                .alojamiento(alojamiento)
                .fechaAgregado(LocalDateTime.now())
                .build();

        favoritoDTO = FavoritoDTO.builder()
                .userId(1)
                .lodgingId(10)
                .build();
    }

    // 1️⃣ Agregar favorito con éxito
    @Test
    void testAgregarFavorito_Exito() {
        when(favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(1, 10)).thenReturn(false);
        when(favoritoMapper.toEntity(favoritoDTO)).thenReturn(favoritoEntity);
        when(favoritoRepository.save(favoritoEntity)).thenReturn(favoritoEntity);
        when(favoritoMapper.toDTO(favoritoEntity)).thenReturn(favoritoDTO);

        FavoritoDTO resultado = favoritoService.agregarFavorito(favoritoDTO);

        assertNotNull(resultado);
        assertEquals(1, resultado.getUserId());
        assertEquals(10, resultado.getLodgingId());
        verify(favoritoRepository).save(favoritoEntity);
    }

    // 2️⃣ Agregar favorito duplicado
    @Test
    void testAgregarFavorito_Duplicado() {
        when(favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(1, 10)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> favoritoService.agregarFavorito(favoritoDTO));
        verify(favoritoRepository, never()).save(any());
    }

    // 3️⃣ Listar favoritos por usuario
    @Test
    void testListarPorUsuario() {
        when(favoritoRepository.findByUsuario_IdOrderByFechaAgregadoDesc(1))
                .thenReturn(List.of(favoritoEntity));
        when(favoritoMapper.toDTO(favoritoEntity)).thenReturn(favoritoDTO);

        List<FavoritoDTO> lista = favoritoService.listarPorUsuario(1);

        assertEquals(1, lista.size());
        assertEquals(10, lista.get(0).getLodgingId());
    }

    // 4️⃣ Contar favoritos por alojamiento
    @Test
    void testContarFavoritosPorAlojamiento() {
        when(favoritoRepository.countByAlojamiento_Id(10)).thenReturn(5L);

        Long count = favoritoService.contarFavoritosPorAlojamiento(10);

        assertEquals(5L, count);
        verify(favoritoRepository).countByAlojamiento_Id(10);
    }

    // 5️⃣ Eliminar favorito existente
    @Test
    void testEliminarFavorito_Existe() {
        when(favoritoRepository.existsById(100)).thenReturn(true);

        favoritoService.eliminarFavorito(100);

        verify(favoritoRepository).deleteById(100);
    }

    // 6️⃣ Eliminar favorito inexistente
    @Test
    void testEliminarFavorito_NoExiste() {
        when(favoritoRepository.existsById(100)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> favoritoService.eliminarFavorito(100));
    }

    // 7️⃣ Eliminar favorito por usuario y alojamiento existente
    @Test
    void testEliminarFavoritoPorUsuarioYAlojamiento_Existe() {
        when(favoritoRepository.findByUsuario_IdAndAlojamiento_Id(1, 10))
                .thenReturn(Optional.of(favoritoEntity));

        favoritoService.eliminarFavoritoPorUsuarioYAlojamiento(1, 10);

        verify(favoritoRepository).deleteById(favoritoEntity.getId());
    }

    // 8️⃣ Eliminar favorito por usuario y alojamiento inexistente
    @Test
    void testEliminarFavoritoPorUsuarioYAlojamiento_NoExiste() {
        when(favoritoRepository.findByUsuario_IdAndAlojamiento_Id(1, 10))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> favoritoService.eliminarFavoritoPorUsuarioYAlojamiento(1, 10));
    }

    // 9️⃣ Verificar si es favorito (true)
    @Test
    void testEsFavorito_True() {
        when(favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(1, 10)).thenReturn(true);

        boolean esFavorito = favoritoService.esFavorito(1, 10);

        assertTrue(esFavorito);
    }

    // 🔟 Verificar si es favorito (false)
    @Test
    void testEsFavorito_False() {
        when(favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(1, 10)).thenReturn(false);

        boolean esFavorito = favoritoService.esFavorito(1, 10);

        assertFalse(esFavorito);
    }

    // 11️⃣ Listar favoritos usuario sin resultados
    @Test
    void testListarPorUsuario_SinResultados() {
        when(favoritoRepository.findByUsuario_IdOrderByFechaAgregadoDesc(2))
                .thenReturn(Collections.emptyList());

        List<FavoritoDTO> resultado = favoritoService.listarPorUsuario(2);

        assertTrue(resultado.isEmpty());
    }

    // 12️⃣ Contar favoritos sin registros
    @Test
    void testContarFavoritosPorAlojamiento_Cero() {
        when(favoritoRepository.countByAlojamiento_Id(20)).thenReturn(0L);

        Long resultado = favoritoService.contarFavoritosPorAlojamiento(20);

        assertEquals(0L, resultado);
    }

    // 13️⃣ Validar mapper llamado al agregar favorito
    @Test
    void testMapperLlamadoEnAgregarFavorito() {
        when(favoritoRepository.existsByUsuario_IdAndAlojamiento_Id(1, 10)).thenReturn(false);
        when(favoritoMapper.toEntity(any())).thenReturn(favoritoEntity);
        when(favoritoRepository.save(any())).thenReturn(favoritoEntity);
        when(favoritoMapper.toDTO(any())).thenReturn(favoritoDTO);

        favoritoService.agregarFavorito(favoritoDTO);

        verify(favoritoMapper).toEntity(favoritoDTO);
        verify(favoritoMapper).toDTO(favoritoEntity);
    }

    // 14️⃣ Verificar excepción al eliminar favorito sin ID
    @Test
    void testEliminarFavorito_IdNull() {
        assertThrows(IllegalArgumentException.class, () -> favoritoService.eliminarFavorito(null));
    }

    // 15️⃣ Verificar que contarFavoritos llama correctamente al repositorio
    @Test
    void testContarFavoritos_LlamaRepositorio() {
        when(favoritoRepository.countByAlojamiento_Id(10)).thenReturn(2L);

        Long result = favoritoService.contarFavoritosPorAlojamiento(10);

        assertEquals(2L, result);
        verify(favoritoRepository, times(1)).countByAlojamiento_Id(10);
    }
}
