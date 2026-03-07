package com.hoteleria.quantum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketEventService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast when a room changes state.
     * Topic: /topic/habitaciones
     */
    public void notificarCambioHabitacion(Integer habitacionId, String numero,
                                           String estadoAnterior, String estadoNuevo) {
        Map<String, Object> payload = Map.of(
                "tipo", "CAMBIO_ESTADO_HABITACION",
                "habitacionId", habitacionId,
                "numero", numero,
                "estadoAnterior", estadoAnterior,
                "estadoNuevo", estadoNuevo,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/habitaciones", payload);
        log.debug("WS event: habitación {} cambió {} → {}", numero, estadoAnterior, estadoNuevo);
    }

    /**
     * Broadcast when a new estadia is created or changes state.
     * Topic: /topic/estadias
     */
    public void notificarCambioEstadia(Long estadiaId, String codigo, String evento,
                                        String estado) {
        Map<String, Object> payload = Map.of(
                "tipo", evento,
                "estadiaId", estadiaId,
                "codigo", codigo != null ? codigo : "",
                "estado", estado,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/estadias", payload);
        log.debug("WS event: estadía {} ({}), evento={}", codigo, estado, evento);
    }
}
