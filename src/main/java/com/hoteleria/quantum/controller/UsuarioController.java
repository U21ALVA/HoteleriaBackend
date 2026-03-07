package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.dto.ApiResponse;
import com.hoteleria.quantum.dto.UsuarioCreateRequest;
import com.hoteleria.quantum.dto.UsuarioResponse;
import com.hoteleria.quantum.dto.UsuarioUpdateRequest;
import com.hoteleria.quantum.security.UserDetailsImpl;
import com.hoteleria.quantum.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> findAll() {
        List<UsuarioResponse> usuarios = usuarioService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos", usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> findById(@PathVariable Long id) {
        UsuarioResponse response = usuarioService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario encontrado", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(
            @Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse response = usuarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        UsuarioResponse response = usuarioService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        Long adminUserId = getCurrentUserId();
        usuarioService.deactivate(id, adminUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        usuarioService.activate(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario activado", null));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
