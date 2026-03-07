package com.hoteleria.quantum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    @Builder.Default
    private String tipo = "Bearer";

    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
    private Map<String, Object> permisos;
}
