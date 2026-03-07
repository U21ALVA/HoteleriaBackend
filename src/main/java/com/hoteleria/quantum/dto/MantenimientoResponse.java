package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MantenimientoResponse {

    private Long id;
    private Integer habitacionId;
    private String habitacionNumero;
    private String tipo;
    private String descripcion;
    private Long usuarioReportaId;
    private String usuarioReportaNombre;
    private Boolean resuelto;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
    private LocalDate fechaProgramada;
    private String notasResolucion;
}
