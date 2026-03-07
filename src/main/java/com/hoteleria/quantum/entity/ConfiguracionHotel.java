package com.hoteleria.quantum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuracion_hotel")
public class ConfiguracionHotel {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "clave", length = 50)
    private String clave;

    @Column(name = "valor", columnDefinition = "TEXT", nullable = false)
    private String valor;

    @Builder.Default
    @Column(name = "tipo_dato", length = 20, nullable = false)
    private String tipoDato = "STRING";

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
