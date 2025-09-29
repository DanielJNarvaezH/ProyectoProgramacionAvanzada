package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "respuesta_comentario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaComentarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con el comentario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_comentario", nullable = false)
    private ComentarioEntity comentario;

    // Relación con el anfitrión (usuario que responde)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_anfitrion", nullable = false)
    private UsuarioEntity anfitrion;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "fecha_respuesta", nullable = false, updatable = false)
    private LocalDateTime fechaRespuesta = LocalDateTime.now();
}
