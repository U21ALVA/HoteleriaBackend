package com.hoteleria.quantum.entity;

import com.hoteleria.quantum.entity.enums.TipoMantenimiento;
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
@Table(name = "mantenimientos")
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", columnDefinition = "tipo_mantenimiento")
    private TipoMantenimiento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reporta_id")
    private Usuario usuarioReporta;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Builder.Default
    @Column(name = "resuelto")
    private Boolean resuelto = false;

    @CreationTimestamp
    @Column(name = "fecha_reporte", updatable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @Column(name = "notas_resolucion", columnDefinition = "TEXT")
    private String notasResolucion;
}
