package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadiaHabitacionResponse {

    private Long id;
    private Integer habitacionId;
    private String habitacionNumero;
    private Integer habitacionPiso;
    private String categoriaNombre;
    private BigDecimal precioNoche;
    private Integer noches;
    private BigDecimal subtotal;
}
