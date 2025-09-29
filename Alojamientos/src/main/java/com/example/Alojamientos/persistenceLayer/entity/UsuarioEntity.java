package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 255)
    private String contrasena; // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 255)
    private String foto;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private java.sql.Timestamp fechaRegistro;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private java.sql.Timestamp fechaActualizacion;

    // Relaciones se agregan después con otras entidades
    // Ejemplo: @OneToMany(mappedBy = "anfitrion") private List<AlojamientoEntity> alojamientos;

    public enum Rol {
        USUARIO,
        ANFITRION
    }
}
