package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "codigo_recuperacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoRecuperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private Timestamp fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private Timestamp fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;
}
