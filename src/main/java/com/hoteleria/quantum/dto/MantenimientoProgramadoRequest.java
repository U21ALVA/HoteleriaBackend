package com.hoteleria.quantum.dto;

import jakarta.validation.constraints.Min;
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
public class MantenimientoProgramadoRequest {

    private Integer habitacionId;

    private Integer categoriaId;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La recurrencia en días es obligatoria")
    @Min(value = 1, message = "La recurrencia debe ser al menos 1 día")
    private Integer recurrenciaDias;

    @NotNull(message = "La próxima ejecución es obligatoria")
    private LocalDate proximaEjecucion;
}
