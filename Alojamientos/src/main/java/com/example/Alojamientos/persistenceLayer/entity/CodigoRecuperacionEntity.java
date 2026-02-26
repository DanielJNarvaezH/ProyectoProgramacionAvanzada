package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "codigos_recuperacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoRecuperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String correo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private Timestamp fechaExpiracion;

    @Column(nullable = false)
    private boolean usado;

    /**
     * Verifica si el código ya expiró.
     */
    public boolean estaExpirado() {
        return new Timestamp(System.currentTimeMillis()).after(fechaExpiracion);
    }
}