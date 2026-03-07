package com.hoteleria.quantum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitacionRequest {

    @NotBlank(message = "El número de habitación es obligatorio")
    @Size(max = 10, message = "El número no puede exceder 10 caracteres")
    private String numero;

    @NotNull(message = "El piso es obligatorio")
    private Integer piso;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    private String notas;
}
