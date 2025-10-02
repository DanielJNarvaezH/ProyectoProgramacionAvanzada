package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorito",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorito_usuario_alojamiento",
                        columnNames = {"id_usuario", "id_alojamiento"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con usuario (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    // Relación con alojamiento (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_alojamiento", nullable = false)
    private AlojamientoEntity alojamiento;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private LocalDateTime fechaAgregado = LocalDateTime.now();
}
