package com.hoteleria.quantum.service;

import com.hoteleria.quantum.dto.ConfiguracionResponse;
import com.hoteleria.quantum.dto.ConfiguracionUpdateRequest;
import com.hoteleria.quantum.entity.ConfiguracionHotel;
import com.hoteleria.quantum.entity.Usuario;
import com.hoteleria.quantum.repository.ConfiguracionHotelRepository;
import com.hoteleria.quantum.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionHotelRepository configuracionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> findAll() {
        return configuracionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConfiguracionResponse findByClave(String clave) {
        ConfiguracionHotel config = configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Configuración no encontrada con clave: " + clave));
        return toResponse(config);
    }

    @Transactional(readOnly = true)
    public String getValor(String clave) {
        return configuracionRepository.findByClave(clave)
                .map(ConfiguracionHotel::getValor)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Configuración no encontrada con clave: " + clave));
    }

    @Transactional(readOnly = true)
    public BigDecimal getValorDecimal(String clave) {
        String valor = getValor(clave);
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El valor de configuración '" + clave + "' no es un número válido: " + valor);
        }
    }

    @Transactional(readOnly = true)
    public Integer getValorInt(String clave) {
        String valor = getValor(clave);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El valor de configuración '" + clave + "' no es un entero válido: " + valor);
        }
    }

    @Transactional
    public ConfiguracionResponse update(ConfiguracionUpdateRequest request, Long usuarioId) {
        ConfiguracionHotel config = configuracionRepository.findByClave(request.getClave())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Configuración no encontrada con clave: " + request.getClave()));

        String valorAnterior = config.getValor();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + usuarioId));

        config.setValor(request.getValor());
        config.setActualizadoPor(usuario);
        config.setActualizadoEn(LocalDateTime.now());
        config = configuracionRepository.save(config);

        auditService.registrarConDetalles(usuarioId, "ACTUALIZAR_CONFIGURACION",
                "ConfiguracionHotel", null,
                Map.of("clave", request.getClave(),
                        "valorAnterior", valorAnterior,
                        "valorNuevo", request.getValor()));

        log.info("Configuración actualizada: {} = {} (antes: {})",
                request.getClave(), request.getValor(), valorAnterior);
        return toResponse(config);
    }

    private ConfiguracionResponse toResponse(ConfiguracionHotel config) {
        return ConfiguracionResponse.builder()
                .clave(config.getClave())
                .valor(config.getValor())
                .tipoDato(config.getTipoDato())
                .descripcion(config.getDescripcion())
                .actualizadoEn(config.getActualizadoEn())
                .build();
    }
}
