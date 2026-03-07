package com.hoteleria.quantum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "estadia_habitaciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"estadia_id", "habitacion_id"})
})
public class EstadiaHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_id")
    private Estadia estadia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @Column(name = "precio_noche", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioNoche;

    @Column(name = "noches", nullable = false)
    private Integer noches;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;
}
