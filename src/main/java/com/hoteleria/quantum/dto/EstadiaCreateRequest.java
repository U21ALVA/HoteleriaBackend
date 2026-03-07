package com.hoteleria.quantum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadiaCreateRequest {

    private Long huespedId;

    @Valid
    private HuespedRequest huesped;

    @NotEmpty(message = "Debe seleccionar al menos una habitación")
    private List<Integer> habitacionIds;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotNull(message = "La fecha de checkout estimado es obligatoria")
    private LocalDateTime fechaCheckoutEstimado;

    @NotNull(message = "El número de noches es obligatorio")
    @Min(value = 1, message = "Debe ser al menos 1 noche")
    private Integer noches;

    @Builder.Default
    private BigDecimal depositoGarantia = BigDecimal.ZERO;

    private String notas;
}
