package com.hoteleria.quantum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitacionEstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private String motivo;
}
