package com.hoteleria.quantum.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "mantenimiento_programado")
public class MantenimientoProgramado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaHabitacion categoria;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @Column(name = "recurrencia_dias", nullable = false)
    private Integer recurrenciaDias;

    @Column(name = "ultima_ejecucion")
    private LocalDate ultimaEjecucion;

    @Column(name = "proxima_ejecucion", nullable = false)
    private LocalDate proximaEjecucion;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}
