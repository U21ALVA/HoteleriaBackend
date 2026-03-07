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
public class HuespedResponse {

    private Long id;
    private String documentoIdentidad;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private LocalDateTime creadoEn;
}
