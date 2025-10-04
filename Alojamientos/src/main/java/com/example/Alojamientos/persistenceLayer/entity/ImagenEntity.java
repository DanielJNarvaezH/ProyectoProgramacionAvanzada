package com.example.Alojamientos.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_alojamiento", nullable = false)
    private AlojamientoEntity alojamiento;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 200)
    private String descripcion;

    @Column(name = "orden_visualizacion", nullable = false)
    @Builder.Default
    private Integer ordenVisualizacion = 0;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
    }
}
