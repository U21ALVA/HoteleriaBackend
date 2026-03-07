package com.hoteleria.quantum.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cardex_cierre")
public class CardexCierre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Builder.Default
    @Column(name = "total_ingresos", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_egresos", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "balance", precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Type(JsonType.class)
    @Column(name = "desglose_metodos", columnDefinition = "jsonb")
    private Map<String, Object> desgloseMetodos;

    @Builder.Default
    @Column(name = "estadias_registradas")
    private Integer estadiasRegistradas = 0;

    @Builder.Default
    @Column(name = "checkouts_realizados")
    private Integer checkoutsRealizados = 0;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_cierre", updatable = false)
    private LocalDateTime fechaCierre;
}
