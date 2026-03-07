package com.hoteleria.quantum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckinRequest {

    @NotNull(message = "El monto de anticipo es obligatorio")
    private BigDecimal anticipoMonto;

    @NotBlank(message = "El método de pago del anticipo es obligatorio")
    private String anticipoMetodoPago;

    private BigDecimal depositoGarantia;

    private String numeroOperacion;
}
