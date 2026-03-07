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
public class ConfiguracionUpdateRequest {

    @NotBlank(message = "La clave es obligatoria")
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    private String valor;
}
