package com.hoteleria.quantum.entity;

import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "habitaciones")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "numero", length = 10, unique = true, nullable = false)
    private String numero;

    @Column(name = "piso", nullable = false)
    private Integer piso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaHabitacion categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "estado_habitacion")
    private EstadoHabitacion estado;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
