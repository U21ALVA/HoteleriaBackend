package com.hoteleria.quantum.dto;

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
public class EstadiaResponse {

    private Long id;
    private String codigo;
    private String estado;
    private String origen;

    private Long huespedId;
    private String huespedNombre;
    private String huespedDocumento;

    private Long usuarioRegistroId;
    private String usuarioRegistroNombre;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaCheckin;
    private LocalDateTime fechaCheckoutEstimado;
    private LocalDateTime fechaCheckoutReal;

    private BigDecimal precioTotal;
    private BigDecimal depositoGarantia;
    private BigDecimal depositoDevuelto;
    private BigDecimal anticipoRequerido;
    private BigDecimal penalizacion;

    private String notas;

    private List<EstadiaHabitacionResponse> habitaciones;
    private List<PagoResponse> pagos;

    private BigDecimal saldoPendiente;

    private LocalDateTime creadoEn;
}
