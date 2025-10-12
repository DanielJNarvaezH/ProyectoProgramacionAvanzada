package com.example.Alojamientos.Usuario;

import com.example.Alojamientos.businessLayer.dto.UsuarioDTO;
import com.example.Alojamientos.businessLayer.service.UsuarioService;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.UsuarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

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
                .id(1)
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

    // --- 1 ---
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

    // --- 2 ---
    @Test
    void listarUsuarios_vacio() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());
        List<UsuarioDTO> result = usuarioService.listarTodos();
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    // --- 3 ---
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

    // --- 4 ---
    @Test
    void crearUsuario_fallaPorCorreoExistente() {
        when(usuarioRepository.existsByCorreo(usuarioDTO.getEmail())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(usuarioDTO));
        verify(usuarioRepository, never()).save(any());
    }

    // --- 5 ---
    @Test
    void crearUsuario_fallaPorTelefonoExistente() {
        when(usuarioRepository.existsByCorreo(usuarioDTO.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByTelefono(usuarioDTO.getPhone())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(usuarioDTO));
        verify(usuarioRepository, never()).save(any());
    }

    // --- 6 ---
    @Test
    void obtenerUsuarioPorId_exitoso() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getName());
        verify(usuarioRepository, times(1)).findById(1);
    }

    // --- 7 ---
    @Test
    void obtenerUsuarioPorId_noExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.obtenerPorId(99));
    }

    // --- 8 ---
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

    // --- 9 ---
    @Test
    void actualizarUsuario_noExiste() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.actualizarUsuario(1, usuarioDTO));
    }

    // --- 10 ---
    @Test
    void eliminarUsuario_exitoso() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);

        usuarioService.eliminarUsuario(1);

        assertFalse(usuarioEntity.getActivo());
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    // --- 11 ---
    @Test
    void eliminarUsuario_noExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> usuarioService.eliminarUsuario(99));
    }

    // --- 12 ---
    @Test
    void crearUsuario_conDatosInvalidosDebeFallar() {
        UsuarioDTO invalido = UsuarioDTO.builder()
                .name("")
                .email("correo_invalido")
                .build();

        assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(invalido));
    }

    // --- 13 ---
    @Test
    void actualizarUsuario_sinCambiosDebeMantenerDatos() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.actualizarUsuario(1, usuarioDTO);

        assertEquals("Juan Perez", result.getName());
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    // --- 14 ---
    @Test
    void listarUsuarios_devuelveMultiples() {
        UsuarioEntity otro = UsuarioEntity.builder()
                .id(2)
                .nombre("Maria Lopez")
                .correo("maria@correo.com")
                .telefono("5555555")
                .rol(UsuarioEntity.Rol.USUARIO)
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .activo(true)
                .build();

        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioEntity, otro));
        when(usuarioMapper.toDTO(any(UsuarioEntity.class))).thenReturn(usuarioDTO);

        List<UsuarioDTO> result = usuarioService.listarTodos();

        assertEquals(2, result.size());
    }

    // --- 15 ---
    @Test
    void crearUsuario_conRolAdminDebeMantenerlo() {
        usuarioDTO.setRole("ADMIN");
        usuarioEntity.setRol(UsuarioEntity.Rol.ADMIN);

        when(usuarioRepository.existsByCorreo(usuarioDTO.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByTelefono(usuarioDTO.getPhone())).thenReturn(false);
        when(usuarioMapper.toEntity(usuarioDTO)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(usuarioDTO);

        UsuarioDTO result = usuarioService.crearUsuario(usuarioDTO);

        assertEquals("ADMIN", result.getRole());
    }
}
