package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CajaMovimientoResponse {

    private Long id;
    private String tipo;
    private String metodoPago;
    private String concepto;
    private BigDecimal monto;
    private Long usuarioId;
    private String usuarioNombre;
    private Long turnoId;
    private Long estadiaId;
    private String estadiaCodigo;
    private Long pagoId;
    private LocalDateTime fechaMovimiento;
}
