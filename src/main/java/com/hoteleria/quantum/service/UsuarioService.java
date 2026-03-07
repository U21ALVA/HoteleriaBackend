package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.UsuarioCreateRequest;
import com.hoteleria.quantum.dto.UsuarioResponse;
import com.hoteleria.quantum.dto.UsuarioUpdateRequest;
import com.hoteleria.quantum.entity.Rol;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.repository.RolRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        // Force eager load of rol
        usuarios.forEach(u -> {
            if (u.getRol() != null) {
                u.getRol().getNombre();
                u.getRol().getPermisos();
            }
        });
        return usuarios.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));
        // Force eager load
        if (usuario.getRol() != null) {
            usuario.getRol().getNombre();
            usuario.getRol().getPermisos();
        }
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse create(UsuarioCreateRequest request) {
        // Validate email uniqueness
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + request.getEmail());
        }

        // Validate rol exists
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rol no encontrado con id: " + request.getRolId()));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        auditService.registrarConDetalles(usuario.getId(), "CREAR_USUARIO", "Usuario",
                usuario.getId(),
                Map.of("nombre", usuario.getNombre(),
                        "email", usuario.getEmail(),
                        "rol", rol.getNombre()));

        log.info("Usuario creado: {} (id={})", usuario.getNombre(), usuario.getId());
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));

        // If email changes, validate uniqueness
        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + request.getEmail());
        }

        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rol no encontrado con id: " + request.getRolId()));

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setRol(rol);

        // If password provided, encode it
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);

        auditService.registrarConDetalles(id, "ACTUALIZAR_USUARIO", "Usuario", id,
                Map.of("nombre", usuario.getNombre(),
                        "email", usuario.getEmail(),
                        "rol", rol.getNombre()));

        log.info("Usuario actualizado: {} (id={})", usuario.getNombre(), usuario.getId());
        return toResponse(usuario);
    }

    @Transactional
    public void deactivate(Long id, Long adminUserId) {
        if (id.equals(adminUserId)) {
            throw new IllegalStateException("No puede desactivar su propia cuenta");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        auditService.registrar(adminUserId, "DESACTIVAR_USUARIO", "Usuario", id);

        log.info("Usuario desactivado: {} (id={})", usuario.getNombre(), id);
    }

    @Transactional
    public void activate(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        log.info("Usuario activado: {} (id={})", usuario.getNombre(), id);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .permisos(usuario.getRol() != null ? usuario.getRol().getPermisos() : null)
                .activo(usuario.getActivo())
                .build();
    }
}
