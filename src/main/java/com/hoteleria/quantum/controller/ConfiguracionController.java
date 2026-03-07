package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.ConfiguracionResponse;
import com.hoteleria.quantum.dto.ConfiguracionUpdateRequest;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.ConfiguracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/configuracion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfiguracionResponse>>> findAll() {
        List<ConfiguracionResponse> configuraciones = configuracionService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Configuraciones obtenidas", configuraciones));
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ApiResponse<ConfiguracionResponse>> findByClave(@PathVariable String clave) {
        ConfiguracionResponse configuracion = configuracionService.findByClave(clave);
        return ResponseEntity.ok(ApiResponse.ok("Configuración encontrada", configuracion));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ConfiguracionResponse>> update(
            @Valid @RequestBody ConfiguracionUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        ConfiguracionResponse response = configuracionService.update(request, userId);
        return ResponseEntity.ok(ApiResponse.ok("Configuración actualizada", response));
    }
}
