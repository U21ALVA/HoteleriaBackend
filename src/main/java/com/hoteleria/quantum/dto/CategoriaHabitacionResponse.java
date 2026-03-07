package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaHabitacionResponse {

    private Integer id;
    private String nombre;
    private BigDecimal precioBase;
    private Integer capacidad;
    private Map<String, Object> caracteristicas;
    private Boolean activo;
    private LocalDateTime creadoEn;
}
