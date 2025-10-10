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

    @Test
    void obtenerPorId_usuarioNoEncontrado() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.obtenerPorId(99));

        assertEquals("Usuario no encontrado con id: 99", exception.getMessage());
    }
}
