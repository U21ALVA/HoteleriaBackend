package com.hoteleria.quantum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MantenimientoRequest {

    @NotNull(message = "La habitación es obligatoria")
    private Integer habitacionId;

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private LocalDate fechaProgramada;
}
