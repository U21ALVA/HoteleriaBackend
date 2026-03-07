package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogAuditoriaResponse {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String accion;
    private String entidadAfectada;
    private Long entidadId;
    private Map<String, Object> detalles;
    private String ipAddress;
    private LocalDateTime fechaHora;
}
