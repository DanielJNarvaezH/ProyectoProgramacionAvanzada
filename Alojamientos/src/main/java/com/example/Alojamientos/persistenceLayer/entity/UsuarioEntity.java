package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private java.sql.Timestamp fechaRegistro;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private java.sql.Timestamp fechaActualizacion;

    // ==========================
    // Relaciones
    // ==========================

    // 1 usuario (anfitrión) -> muchos alojamientos
    @OneToMany(mappedBy = "anfitrion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlojamientoEntity> alojamientos;

    // 1 usuario (huésped) -> muchas reservas
    @OneToMany(mappedBy = "huesped", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaEntity> reservas;

    // 1 usuario -> muchos comentarios
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComentarioEntity> comentarios;

    // 1 usuario -> muchas respuestas a comentarios
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespuestaComentarioEntity> respuestas;

    // 1 usuario -> muchos códigos de recuperación
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodigoRecuperacionEntity> codigosRecuperacion;

    // 1 usuario -> muchas notificaciones
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificacionEntity> notificaciones;

    // 1 usuario -> muchos favorito
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoritoEntity> favorito;

    public enum Rol {
        USUARIO,
        ANFITRION,
        ADMIN
    }
}
