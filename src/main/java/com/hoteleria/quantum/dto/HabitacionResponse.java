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
public class HabitacionResponse {

    private Integer id;
    private String numero;
    private Integer piso;
    private String estado;
    private String notas;
    private Boolean activo;

    private Integer categoriaId;
    private String categoriaNombre;
    private BigDecimal precioBase;
    private Integer capacidad;

    private LocalDateTime creadoEn;
}
