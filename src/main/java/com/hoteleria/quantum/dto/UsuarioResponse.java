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
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private Map<String, Object> permisos;
    private Boolean activo;
}
