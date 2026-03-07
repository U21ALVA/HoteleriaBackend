package com.hoteleria.quantum.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "accion", length = 100, nullable = false)
    private String accion;

    @Column(name = "entidad_afectada", length = 50, nullable = false)
    private String entidadAfectada;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Type(JsonType.class)
    @Column(name = "detalles", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> detalles;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;
}
