package com.hoteleria.quantum.controller;

import com.hoteleria.quantum.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteController {

    private final ReporteService reporteService;

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping("/ocupacion")
    public ResponseEntity<byte[]> reporteOcupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            byte[] archivo = reporteService.generarReporteOcupacion(desde, hasta);
            String filename = "reporte_ocupacion_" + LocalDate.now() + ".xlsx";
            return buildFileResponse(archivo, filename, EXCEL_CONTENT_TYPE);
        } catch (Exception e) {
            log.error("Error generando reporte de ocupación", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ingresos")
    public ResponseEntity<byte[]> reporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            byte[] archivo = reporteService.generarReporteIngresos(desde, hasta);
            String filename = "reporte_ingresos_" + LocalDate.now() + ".xlsx";
            return buildFileResponse(archivo, filename, EXCEL_CONTENT_TYPE);
        } catch (Exception e) {
            log.error("Error generando reporte de ingresos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estadias")
    public ResponseEntity<byte[]> reporteEstadias(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            byte[] archivo = reporteService.generarReporteEstadias(desde, hasta);
            String filename = "reporte_estadias_" + LocalDate.now() + ".pdf";
            return buildFileResponse(archivo, filename, MediaType.APPLICATION_PDF_VALUE);
        } catch (Exception e) {
            log.error("Error generando reporte de estadías", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/auditoria")
    public ResponseEntity<byte[]> reporteAuditoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            byte[] archivo = reporteService.generarReporteAuditoria(desde, hasta);
            String filename = "reporte_auditoria_" + LocalDate.now() + ".pdf";
            return buildFileResponse(archivo, filename, MediaType.APPLICATION_PDF_VALUE);
        } catch (Exception e) {
            log.error("Error generando reporte de auditoría", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] fileContent, String filename,
                                                      String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentLength(fileContent.length);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}
