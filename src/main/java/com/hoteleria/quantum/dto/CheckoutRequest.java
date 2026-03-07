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
public class CheckoutRequest {

    private BigDecimal montoFinal;

    private String metodoPago;

    private String numeroOperacion;

    @Builder.Default
    private Boolean devolverDeposito = true;
}
