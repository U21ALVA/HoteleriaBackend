package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    private BigDecimal ingresosDia;
    private BigDecimal ingresosMes;
    private Integer estadiasActivas;
    private Integer habitacionesOcupadas;
    private Integer habitacionesDisponibles;
    private Integer checkoutsHoy;
    private BigDecimal ocupacionPorcentaje;
    private Map<String, BigDecimal> ingresosPorMetodo;
}
