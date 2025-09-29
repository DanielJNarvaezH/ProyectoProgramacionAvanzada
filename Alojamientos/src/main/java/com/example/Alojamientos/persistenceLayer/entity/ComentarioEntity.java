package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con reserva (cada comentario está asociado a una reserva)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private ReservaEntity reserva;

    // Relación con usuario (quien hace el comentario)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private Integer calificacion; // entre 1 y 5

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "fecha_comentario", nullable = false, updatable = false)
    private LocalDateTime fechaComentario = LocalDateTime.now();
}
