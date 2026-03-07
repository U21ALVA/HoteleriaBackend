package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardexCierreResponse {

    private Long id;
    private LocalDate fecha;
    private Long turnoId;
    private Long usuarioCierreId;
    private String usuarioCierreNombre;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal balance;
    private Map<String, Object> desgloseMetodos;
    private Integer estadiasRegistradas;
    private Integer checkoutsRealizados;
    private String observaciones;
    private LocalDateTime fechaCierre;
}
