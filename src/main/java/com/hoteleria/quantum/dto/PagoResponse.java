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
public class PagoResponse {

    private Long id;
    private Long estadiaId;
    private String estadiaCodigo;
    private String metodoPago;
    private BigDecimal monto;
    private String numeroOperacion;
    private String concepto;
    private Long usuarioRegistroId;
    private String usuarioRegistroNombre;
    private Long turnoId;
    private LocalDateTime fechaPago;
}
