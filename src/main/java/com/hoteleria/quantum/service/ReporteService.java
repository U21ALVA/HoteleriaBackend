package com.hoteleria.quantum.service;

import com.hoteleria.quantum.entity.*;
import com.hoteleria.quantum.entity.enums.*;
import com.hoteleria.quantum.repository.*;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final HabitacionRepository habitacionRepository;
    private final EstadiaRepository estadiaRepository;
    private final EstadiaHabitacionRepository estadiaHabitacionRepository;
    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final PagoRepository pagoRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ───────────────────────────────────────────────
    // 1. Reporte de Ocupación (Excel)
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteOcupacion(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte de ocupación: {} - {}", desde, hasta);

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<Habitacion> habitaciones = habitacionRepository.findAll();
        List<Estadia> estadias = estadiaRepository.findByFechaRegistroBetween(inicio, fin);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle moneyStyle = crearEstiloDinero(workbook);
            CellStyle percentStyle = crearEstiloPorcentaje(workbook);

            // ── Sheet 1: Resumen ──
            Sheet resumen = workbook.createSheet("Resumen");
            crearSheetResumenOcupacion(resumen, headerStyle, habitaciones, desde, hasta);

            // ── Sheet 2: Detalle Habitaciones ──
            Sheet detalle = workbook.createSheet("Detalle Habitaciones");
            crearSheetDetalleHabitaciones(detalle, headerStyle, habitaciones);

            // ── Sheet 3: Estadías del Período ──
            Sheet estadiasSheet = workbook.createSheet("Estadías del Período");
            crearSheetEstadiasDelPeriodo(estadiasSheet, headerStyle, dateStyle, moneyStyle, estadias);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Error generando reporte de ocupación", e);
            throw new RuntimeException("Error generando reporte de ocupación: " + e.getMessage(), e);
        }
    }

    private void crearSheetResumenOcupacion(Sheet sheet, CellStyle headerStyle,
                                            List<Habitacion> habitaciones,
                                            LocalDate desde, LocalDate hasta) {
        int rowIdx = 0;

        // Título
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Ocupación - Hotelería Quantum");
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        org.apache.poi.ss.usermodel.Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // Período
        Row periodoRow = sheet.createRow(rowIdx++);
        periodoRow.createCell(0).setCellValue("Período: " + desde.format(DATE_FMT) + " - " + hasta.format(DATE_FMT));
        rowIdx++; // blank row

        // Conteos por estado
        long totalHabitaciones = habitaciones.size();
        long totalActivas = habitaciones.stream().filter(h -> Boolean.TRUE.equals(h.getActivo())).count();

        Row headerRow = sheet.createRow(rowIdx++);
        crearCeldaHeader(headerRow, 0, "Estado", headerStyle);
        crearCeldaHeader(headerRow, 1, "Cantidad", headerStyle);
        crearCeldaHeader(headerRow, 2, "% del Total", headerStyle);

        for (EstadoHabitacion estado : EstadoHabitacion.values()) {
            long count = habitaciones.stream()
                    .filter(h -> h.getEstado() == estado && Boolean.TRUE.equals(h.getActivo()))
                    .count();
            Row dataRow = sheet.createRow(rowIdx++);
            dataRow.createCell(0).setCellValue(estado.name());
            dataRow.createCell(1).setCellValue(count);
            if (totalActivas > 0) {
                dataRow.createCell(2).setCellValue(
                        String.format("%.1f%%", (count * 100.0) / totalActivas));
            } else {
                dataRow.createCell(2).setCellValue("0.0%");
            }
        }

        rowIdx++; // blank row
        Row totalRow = sheet.createRow(rowIdx++);
        totalRow.createCell(0).setCellValue("Total Habitaciones");
        totalRow.createCell(1).setCellValue(totalHabitaciones);

        Row activasRow = sheet.createRow(rowIdx);
        activasRow.createCell(0).setCellValue("Habitaciones Activas");
        activasRow.createCell(1).setCellValue(totalActivas);

        autoSizeColumns(sheet, 3);
    }

    private void crearSheetDetalleHabitaciones(Sheet sheet, CellStyle headerStyle,
                                               List<Habitacion> habitaciones) {
        String[] headers = {"Número", "Piso", "Categoría", "Estado", "Activo"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            crearCeldaHeader(headerRow, i, headers[i], headerStyle);
        }

        int rowIdx = 1;
        for (Habitacion h : habitaciones) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(h.getNumero());
            row.createCell(1).setCellValue(h.getPiso());
            row.createCell(2).setCellValue(
                    h.getCategoria() != null ? h.getCategoria().getNombre() : "—");
            row.createCell(3).setCellValue(h.getEstado() != null ? h.getEstado().name() : "—");
            row.createCell(4).setCellValue(Boolean.TRUE.equals(h.getActivo()) ? "Sí" : "No");
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void crearSheetEstadiasDelPeriodo(Sheet sheet, CellStyle headerStyle,
                                              CellStyle dateStyle, CellStyle moneyStyle,
                                              List<Estadia> estadias) {
        String[] headers = {"Código", "Huésped", "Estado", "Fecha Registro",
                "Check-in", "Checkout Estimado", "Checkout Real", "Precio Total"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            crearCeldaHeader(headerRow, i, headers[i], headerStyle);
        }

        int rowIdx = 1;
        BigDecimal sumaTotal = BigDecimal.ZERO;

        for (Estadia e : estadias) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(e.getCodigo() != null ? e.getCodigo() : "—");
            row.createCell(1).setCellValue(
                    e.getHuesped() != null ? e.getHuesped().getNombreCompleto() : "—");
            row.createCell(2).setCellValue(e.getEstado() != null ? e.getEstado().name() : "—");
            crearCeldaFecha(row, 3, e.getFechaRegistro(), dateStyle);
            crearCeldaFecha(row, 4, e.getFechaCheckin(), dateStyle);
            crearCeldaFecha(row, 5, e.getFechaCheckoutEstimado(), dateStyle);
            crearCeldaFecha(row, 6, e.getFechaCheckoutReal(), dateStyle);

            Cell montoCell = row.createCell(7);
            if (e.getPrecioTotal() != null) {
                montoCell.setCellValue(e.getPrecioTotal().doubleValue());
                montoCell.setCellStyle(moneyStyle);
                sumaTotal = sumaTotal.add(e.getPrecioTotal());
            } else {
                montoCell.setCellValue(0.0);
            }
        }

        // Fila de totales
        Row totalRow = sheet.createRow(rowIdx);
        Cell labelCell = totalRow.createCell(6);
        labelCell.setCellValue("TOTAL:");
        org.apache.poi.ss.usermodel.Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
        boldStyle.setFont(boldFont);
        labelCell.setCellStyle(boldStyle);

        Cell totalCell = totalRow.createCell(7);
        totalCell.setCellValue(sumaTotal.doubleValue());
        CellStyle totalMoneyStyle = sheet.getWorkbook().createCellStyle();
        totalMoneyStyle.cloneStyleFrom(moneyStyle);
        totalMoneyStyle.setFont(boldFont);
        totalCell.setCellStyle(totalMoneyStyle);

        autoSizeColumns(sheet, headers.length);
    }

    // ───────────────────────────────────────────────
    // 2. Reporte de Ingresos (Excel)
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteIngresos(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte de ingresos: {} - {}", desde, hasta);

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle moneyStyle = crearEstiloDinero(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            // ── Sheet 1: Resumen ──
            Sheet resumen = workbook.createSheet("Resumen");
            crearSheetResumenIngresos(resumen, headerStyle, moneyStyle, inicio, fin, desde, hasta);

            // ── Sheet 2: Movimientos de Caja ──
            Sheet movimientos = workbook.createSheet("Movimientos de Caja");
            crearSheetMovimientosCaja(movimientos, headerStyle, moneyStyle, dateStyle, inicio, fin);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Error generando reporte de ingresos", e);
            throw new RuntimeException("Error generando reporte de ingresos: " + e.getMessage(), e);
        }
    }

    private void crearSheetResumenIngresos(Sheet sheet, CellStyle headerStyle,
                                           CellStyle moneyStyle,
                                           LocalDateTime inicio, LocalDateTime fin,
                                           LocalDate desde, LocalDate hasta) {
        int rowIdx = 0;

        // Título
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte de Ingresos - Hotelería Quantum");
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        org.apache.poi.ss.usermodel.Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        Row periodoRow = sheet.createRow(rowIdx++);
        periodoRow.createCell(0).setCellValue("Período: " + desde.format(DATE_FMT) + " - " + hasta.format(DATE_FMT));
        rowIdx++; // blank

        // Total ingresos y egresos
        BigDecimal totalIngresos = Optional.ofNullable(
                cajaMovimientoRepository.sumMontoByTipoAndFechaMovimientoBetween(
                        TipoMovimientoCaja.INGRESO, inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal totalEgresos = Optional.ofNullable(
                cajaMovimientoRepository.sumMontoByTipoAndFechaMovimientoBetween(
                        TipoMovimientoCaja.EGRESO, inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal balance = totalIngresos.subtract(totalEgresos);

        Row ingresosRow = sheet.createRow(rowIdx++);
        ingresosRow.createCell(0).setCellValue("Total Ingresos");
        Cell ingresosCell = ingresosRow.createCell(1);
        ingresosCell.setCellValue(totalIngresos.doubleValue());
        ingresosCell.setCellStyle(moneyStyle);

        Row egresosRow = sheet.createRow(rowIdx++);
        egresosRow.createCell(0).setCellValue("Total Egresos");
        Cell egresosCell = egresosRow.createCell(1);
        egresosCell.setCellValue(totalEgresos.doubleValue());
        egresosCell.setCellStyle(moneyStyle);

        Row balanceRow = sheet.createRow(rowIdx++);
        balanceRow.createCell(0).setCellValue("Balance");
        Cell balanceCell = balanceRow.createCell(1);
        balanceCell.setCellValue(balance.doubleValue());
        org.apache.poi.ss.usermodel.Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBold(true);
        CellStyle balanceStyle = sheet.getWorkbook().createCellStyle();
        balanceStyle.cloneStyleFrom(moneyStyle);
        balanceStyle.setFont(boldFont);
        balanceCell.setCellStyle(balanceStyle);

        rowIdx++; // blank

        // Desglose por método de pago
        Row metodoHeader = sheet.createRow(rowIdx++);
        crearCeldaHeader(metodoHeader, 0, "Método de Pago", headerStyle);
        crearCeldaHeader(metodoHeader, 1, "Monto", headerStyle);

        for (MetodoPago metodo : MetodoPago.values()) {
            BigDecimal monto = Optional.ofNullable(
                    pagoRepository.sumMontoByMetodoPagoAndFechaPagoBetween(metodo, inicio, fin))
                    .orElse(BigDecimal.ZERO);

            Row metodoRow = sheet.createRow(rowIdx++);
            metodoRow.createCell(0).setCellValue(metodo.name());
            Cell montoCell = metodoRow.createCell(1);
            montoCell.setCellValue(monto.doubleValue());
            montoCell.setCellStyle(moneyStyle);
        }

        autoSizeColumns(sheet, 4);
    }

    private void crearSheetMovimientosCaja(Sheet sheet, CellStyle headerStyle,
                                           CellStyle moneyStyle, CellStyle dateStyle,
                                           LocalDateTime inicio, LocalDateTime fin) {
        String[] headers = {"Tipo", "Método Pago", "Concepto", "Monto", "Usuario", "Fecha"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            crearCeldaHeader(headerRow, i, headers[i], headerStyle);
        }

        List<CajaMovimiento> movimientos = cajaMovimientoRepository.findByFechaMovimientoBetween(inicio, fin);

        int rowIdx = 1;
        BigDecimal sumaIngresos = BigDecimal.ZERO;
        BigDecimal sumaEgresos = BigDecimal.ZERO;

        for (CajaMovimiento mov : movimientos) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(mov.getTipo() != null ? mov.getTipo().name() : "—");
            row.createCell(1).setCellValue(mov.getMetodoPago() != null ? mov.getMetodoPago().name() : "—");
            row.createCell(2).setCellValue(mov.getConcepto() != null ? mov.getConcepto() : "—");

            Cell montoCell = row.createCell(3);
            if (mov.getMonto() != null) {
                montoCell.setCellValue(mov.getMonto().doubleValue());
                montoCell.setCellStyle(moneyStyle);

                if (mov.getTipo() == TipoMovimientoCaja.INGRESO) {
                    sumaIngresos = sumaIngresos.add(mov.getMonto());
                } else if (mov.getTipo() == TipoMovimientoCaja.EGRESO) {
                    sumaEgresos = sumaEgresos.add(mov.getMonto());
                }
            }

            row.createCell(4).setCellValue(
                    mov.getUsuario() != null ? mov.getUsuario().getNombre() : "—");
            crearCeldaFecha(row, 5, mov.getFechaMovimiento(), dateStyle);
        }

        // Fila de totales
        rowIdx++; // blank
        org.apache.poi.ss.usermodel.Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBold(true);
        CellStyle totalStyle = sheet.getWorkbook().createCellStyle();
        totalStyle.cloneStyleFrom(moneyStyle);
        totalStyle.setFont(boldFont);

        Row totalIngRow = sheet.createRow(rowIdx++);
        Cell labelIng = totalIngRow.createCell(2);
        labelIng.setCellValue("Total Ingresos:");
        CellStyle boldCellStyle = sheet.getWorkbook().createCellStyle();
        boldCellStyle.setFont(boldFont);
        labelIng.setCellStyle(boldCellStyle);
        Cell totalIngCell = totalIngRow.createCell(3);
        totalIngCell.setCellValue(sumaIngresos.doubleValue());
        totalIngCell.setCellStyle(totalStyle);

        Row totalEgrRow = sheet.createRow(rowIdx);
        Cell labelEgr = totalEgrRow.createCell(2);
        labelEgr.setCellValue("Total Egresos:");
        labelEgr.setCellStyle(boldCellStyle);
        Cell totalEgrCell = totalEgrRow.createCell(3);
        totalEgrCell.setCellValue(sumaEgresos.doubleValue());
        totalEgrCell.setCellStyle(totalStyle);

        autoSizeColumns(sheet, headers.length);
    }

    // ───────────────────────────────────────────────
    // 3. Reporte de Estadías (PDF)
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteEstadias(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte de estadías (PDF): {} - {}", desde, hasta);

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<Estadia> estadias = estadiaRepository.findByFechaRegistroBetween(inicio, fin);

        // Force initialization of lazy associations inside the transaction
        for (Estadia estadia : estadias) {
            if (estadia.getHuesped() != null) {
                estadia.getHuesped().getNombreCompleto();
            }
        }

        // Load estadia-habitacion mappings within the transaction
        List<List<EstadiaHabitacion>> estadiaHabitacionesList = estadias.stream()
                .map(e -> {
                    List<EstadiaHabitacion> ehs = estadiaHabitacionRepository.findByEstadiaId(e.getId());
                    // Force initialization of lazy habitacion
                    ehs.forEach(eh -> {
                        if (eh.getHabitacion() != null) {
                            eh.getHabitacion().getNumero();
                        }
                    });
                    return ehs;
                })
                .toList();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.DARK_GRAY);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY);
            Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font tableBodyFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
            Font totalFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);

            // Título
            Paragraph title = new Paragraph("Reporte de Estadías - Hotelería Quantum", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Período: " + desde.format(DATE_FMT) + " - " + hasta.format(DATE_FMT), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            document.add(subtitle);

            // Tabla
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.5f, 2f, 1.3f, 1.8f, 1.8f, 1.5f});

            String[] headers = {"Código", "Huésped", "Habitación(es)", "Estado",
                    "Check-in", "Checkout", "Precio Total"};
            Color headerBg = new Color(41, 65, 122);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            BigDecimal sumaTotal = BigDecimal.ZERO;
            Color altRowColor = new Color(240, 244, 250);

            for (int i = 0; i < estadias.size(); i++) {
                Estadia e = estadias.get(i);
                List<EstadiaHabitacion> habitacionesEstadia = estadiaHabitacionesList.get(i);
                Color rowColor = (i % 2 == 0) ? Color.WHITE : altRowColor;

                // Código
                agregarCeldaPdf(table, e.getCodigo() != null ? e.getCodigo() : "—",
                        tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Huésped
                agregarCeldaPdf(table, e.getHuesped() != null ? e.getHuesped().getNombreCompleto() : "—",
                        tableBodyFont, rowColor, Element.ALIGN_LEFT);

                // Habitaciones
                String habitacionesStr = habitacionesEstadia.stream()
                        .map(eh -> eh.getHabitacion() != null ? eh.getHabitacion().getNumero() : "—")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("—");
                agregarCeldaPdf(table, habitacionesStr, tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Estado
                agregarCeldaPdf(table, e.getEstado() != null ? e.getEstado().name() : "—",
                        tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Check-in
                agregarCeldaPdf(table, formatDateTime(e.getFechaCheckin()),
                        tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Checkout (real si existe, sino estimado)
                String checkoutStr = e.getFechaCheckoutReal() != null
                        ? formatDateTime(e.getFechaCheckoutReal())
                        : formatDateTime(e.getFechaCheckoutEstimado());
                agregarCeldaPdf(table, checkoutStr, tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Precio Total
                BigDecimal precio = e.getPrecioTotal() != null ? e.getPrecioTotal() : BigDecimal.ZERO;
                agregarCeldaPdf(table, formatMoney(precio), tableBodyFont, rowColor, Element.ALIGN_RIGHT);
                sumaTotal = sumaTotal.add(precio);
            }

            document.add(table);

            // Footer con totales
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(12);
            footer.add(new Chunk("Total estadías: " + estadias.size() + "    |    ", totalFont));
            footer.add(new Chunk("Suma total: S/ " + formatMoney(sumaTotal), totalFont));
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Error generando reporte de estadías (PDF)", e);
            throw new RuntimeException("Error generando reporte de estadías: " + e.getMessage(), e);
        }
    }

    // ───────────────────────────────────────────────
    // 4. Reporte de Auditoría (PDF)
    // ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteAuditoria(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte de auditoría (PDF): {} - {}", desde, hasta);

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<LogAuditoria> logs = logAuditoriaRepository.findByFechaHoraBetween(inicio, fin);

        // Force initialization of lazy usuario association
        for (LogAuditoria logEntry : logs) {
            if (logEntry.getUsuario() != null) {
                logEntry.getUsuario().getNombre();
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.DARK_GRAY);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY);
            Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font tableBodyFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
            Font totalFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);

            // Título
            Paragraph title = new Paragraph("Reporte de Auditoría - Hotelería Quantum", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Período: " + desde.format(DATE_FMT) + " - " + hasta.format(DATE_FMT), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(16);
            document.add(subtitle);

            // Tabla
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.8f, 2f, 1.5f, 3f});

            String[] headers = {"Fecha/Hora", "Usuario", "Acción", "Entidad", "Detalle"};
            Color headerBg = new Color(41, 65, 122);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            Color altRowColor = new Color(240, 244, 250);

            for (int i = 0; i < logs.size(); i++) {
                LogAuditoria logEntry = logs.get(i);
                Color rowColor = (i % 2 == 0) ? Color.WHITE : altRowColor;

                // Fecha/Hora
                agregarCeldaPdf(table, formatDateTime(logEntry.getFechaHora()),
                        tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Usuario
                agregarCeldaPdf(table,
                        logEntry.getUsuario() != null ? logEntry.getUsuario().getNombre() : "Sistema",
                        tableBodyFont, rowColor, Element.ALIGN_LEFT);

                // Acción
                agregarCeldaPdf(table, logEntry.getAccion() != null ? logEntry.getAccion() : "—",
                        tableBodyFont, rowColor, Element.ALIGN_LEFT);

                // Entidad
                agregarCeldaPdf(table,
                        logEntry.getEntidadAfectada() != null ? logEntry.getEntidadAfectada() : "—",
                        tableBodyFont, rowColor, Element.ALIGN_CENTER);

                // Detalle
                String detalle = logEntry.getDetalles() != null
                        ? logEntry.getDetalles().toString()
                        : "—";
                // Truncate very long detail strings for readability
                if (detalle.length() > 200) {
                    detalle = detalle.substring(0, 197) + "...";
                }
                agregarCeldaPdf(table, detalle, tableBodyFont, rowColor, Element.ALIGN_LEFT);
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph(
                    "Total registros: " + logs.size(), totalFont);
            footer.setSpacingBefore(12);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Error generando reporte de auditoría (PDF)", e);
            throw new RuntimeException("Error generando reporte de auditoría: " + e.getMessage(), e);
        }
    }

    // ───────────────────────────────────────────────
    // Utilidades Excel
    // ───────────────────────────────────────────────

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloDinero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle crearEstiloPorcentaje(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void crearCeldaHeader(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void crearCeldaFecha(Row row, int col, LocalDateTime dateTime, CellStyle style) {
        Cell cell = row.createCell(col);
        if (dateTime != null) {
            cell.setCellValue(dateTime.format(DATETIME_FMT));
        } else {
            cell.setCellValue("—");
        }
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ───────────────────────────────────────────────
    // Utilidades PDF
    // ───────────────────────────────────────────────

    private void agregarCeldaPdf(PdfPTable table, String text, Font font,
                                 Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATETIME_FMT) : "—";
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
