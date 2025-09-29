package com.example.Alojamientos;


@SpringBootTest
class EntidadTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void crearUsuarioTest() {
        Usuario usuario = Usuario.builder()
                .nombre("Prueba")
                .correo("prueba@email.com")
                .telefono("123456789")
                .contrasena("hashedpassword")
                .rol("USUARIO")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        assertNotNull(guardado.getId());
    }
}
