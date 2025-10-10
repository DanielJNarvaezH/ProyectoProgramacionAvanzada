package com.example.Alojamientos.Usuario;

import com.example.Alojamientos.businessLayer.service.UsuarioService;
import com.example.Alojamientos.businessLayer.dto.UsuarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.UsuarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioDataMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuarioDTO = UsuarioDTO.builder()
                .name("Juan Perez")
                .email("juan@correo.com")
                .phone("123456789")
                .password("Abc12345")
                .birthDate(LocalDate.of(1990, 1, 1).toString())
                .role("USUARIO")
                .build();

        usuarioEntity = UsuarioEntity.builder()
                .id(1)
                .nombre("Juan Perez")
                .correo("juan@correo.com")
                .telefono("123456789")
                .contrasena("Abc12345")
                .rol(UsuarioEntity.Rol.USUARIO)
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .activo(true)
                .build();
    }

    // --- GET /api/usuarios ---
    @Test
    void listarUsuarios_exitoso() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioEntity));
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        List<UsuarioDTO> result = usuarioService.listarTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getName());
        verify(usuarioRepository, times(1)).findAll();
    }

    // --- POST /api/usuarios ---
    @Test
    void crearUsuario_exitoso() {
        when(usuarioRepository.existsByCorreo(usuarioDTO.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByTelefono(usuarioDTO.getPhone())).thenReturn(false);
        when(usuarioMapper.toEntity(usuarioDTO)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.crearUsuario(usuarioDTO);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getName());
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    // --- GET /api/usuarios/{id} ---
    @Test
    void obtenerUsuarioPorId_exitoso() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getName());
        verify(usuarioRepository, times(1)).findById(1);
    }

    // --- PUT /api/usuarios/{id} ---
    @Test
    void actualizarUsuario_exitoso() {
        UsuarioDTO actualizado = UsuarioDTO.builder()
                .id(1)
                .name("Juan Actualizado")
                .email("juan@correo.com")
                .phone("987654321")
                .password("NuevaPass123")
                .birthDate(LocalDate.of(1990, 1, 1).toString())
                .role("USUARIO")
                .build();

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioEntity);
        when(usuarioMapper.toDTO(any(UsuarioEntity.class))).thenReturn(actualizado);

        UsuarioDTO result = usuarioService.actualizarUsuario(1, actualizado);

        assertNotNull(result);
        assertEquals("Juan Actualizado", result.getName());
        verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
    }

    // --- DELETE /api/usuarios/{id} ---
    @Test
    void eliminarUsuario_exitoso() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);

        usuarioService.eliminarUsuario(1);

        assertFalse(usuarioEntity.getActivo()); // se debe marcar como inactivo
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }
}
