package com.hoteleria.quantum.entity;

import com.hoteleria.quantum.entity.enums.EstadoEstadia;
import com.hoteleria.quantum.entity.enums.OrigenEstadia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "estadias")
public class Estadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", length = 20, unique = true, nullable = false, insertable = false, updatable = false)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id")
    private Huesped huesped;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", columnDefinition = "origen_estadia")
    private OrigenEstadia origen;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_checkin")
    private LocalDateTime fechaCheckin;

    @Column(name = "fecha_checkout_estimado", nullable = false)
    private LocalDateTime fechaCheckoutEstimado;

    @Column(name = "fecha_checkout_real")
    private LocalDateTime fechaCheckoutReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "estado_estadia")
    private EstadoEstadia estado;

    @Column(name = "precio_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioTotal;

    @Builder.Default
    @Column(name = "deposito_garantia", precision = 10, scale = 2)
    private BigDecimal depositoGarantia = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "deposito_devuelto")
    private Boolean depositoDevuelto = false;

    @Column(name = "anticipo_requerido", precision = 10, scale = 2, nullable = false)
    private BigDecimal anticipoRequerido;

    @Builder.Default
    @Column(name = "penalizacion", precision = 10, scale = 2)
    private BigDecimal penalizacion = BigDecimal.ZERO;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
