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
public class MantenimientoProgramadoResponse {

    private Long id;
    private Integer habitacionId;
    private String habitacionNumero;
    private Integer categoriaId;
    private String categoriaNombre;
    private String descripcion;
    private Integer recurrenciaDias;
    private LocalDate ultimaEjecucion;
    private LocalDate proximaEjecucion;
    private Boolean activo;
    private LocalDateTime creadoEn;
}
