package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.LoginRequest;
import com.hoteleria.quantum.dto.LoginResponse;
import com.hoteleria.quantum.dto.UsuarioResponse;
import com.hoteleria.quantum.entity.IntentoLogin;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.repository.IntentoLoginRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import com.hoteleria.quantum.security.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UsuarioRepository usuarioRepository;
    private final IntentoLoginRepository intentoLoginRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtProvider.generateToken(request.getEmail());

            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Usuario no encontrado con email: " + request.getEmail()));

            // Registrar intento exitoso
            intentoLoginRepository.save(IntentoLogin.builder()
                    .email(request.getEmail())
                    .exitoso(true)
                    .ipAddress(ipAddress)
                    .build());

            log.info("Login exitoso para usuario: {}", request.getEmail());

            return LoginResponse.builder()
                    .token(jwt)
                    .usuarioId(usuario.getId())
                    .nombre(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol().getNombre())
                    .permisos(usuario.getRol().getPermisos())
                    .build();

        } catch (BadCredentialsException e) {
            // Registrar intento fallido
            intentoLoginRepository.save(IntentoLogin.builder()
                    .email(request.getEmail())
                    .exitoso(false)
                    .ipAddress(ipAddress)
                    .mensajeError("Credenciales inválidas")
                    .build());

            log.warn("Intento de login fallido para: {}", request.getEmail());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con email: " + email));

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().getNombre())
                .permisos(usuario.getRol().getPermisos())
                .activo(usuario.getActivo())
                .build();
    }
}
