package com.hoteleria.quantum.entity;

import com.hoteleria.quantum.entity.enums.EstadoHabitacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "habitacion_estado_historial")
public class HabitacionEstadoHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", columnDefinition = "estado_habitacion")
    private EstadoHabitacion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", columnDefinition = "estado_habitacion", nullable = false)
    private EstadoHabitacion estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;
}
