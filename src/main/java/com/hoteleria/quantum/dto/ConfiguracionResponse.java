package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionResponse {

    private String clave;
    private String valor;
    private String tipoDato;
    private String descripcion;
    private LocalDateTime actualizadoEn;
}
