package com.hoteleria.quantum.entity;

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
@Table(name = "intentos_login")
public class IntentoLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mensaje_error", length = 255)
    private String mensajeError;

    @CreationTimestamp
    @Column(name = "fecha_intento", updatable = false)
    private LocalDateTime fechaIntento;
}
