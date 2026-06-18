package com.GesCom.service.Impl;

import com.GesCom.dto.response.*;
import com.GesCom.service.ReportePdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ReportePdfServiceImpl implements ReportePdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("$ #,##0.00");

    private Font titleFont() throws Exception {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    }
    private Font subtitleFont() throws Exception {
        return FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, new Color(100, 100, 100));
    }
    private Font headerFont() throws Exception {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, BaseColor.WHITE);
    }
    private Font cellFont() throws Exception {
        return FontFactory.getFont(FontFactory.HELVETICA, 7);
    }
    private Font boldFont() throws Exception {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    }

    private void addHeader(Document doc, String titulo, String subtitulo) throws Exception {
        Paragraph p = new Paragraph(titulo, titleFont());
        p.setSpacingAfter(2);
        doc.add(p);
        Paragraph s = new Paragraph(subtitulo, subtitleFont());
        s.setSpacingAfter(12);
        doc.add(s);
    }

    private PdfPTable createTable(String[] headers, float[] widths) throws Exception {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setSpacingBefore(4);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont()));
            cell.setBackgroundColor(new Color(93, 135, 255));
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    private void addCell(PdfPTable table, String text, Font font, int align) throws Exception {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addCellAlt(PdfPTable table, String text, Font font, int align, boolean alt) throws Exception {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        cell.setHorizontalAlignment(align);
        if (alt) cell.setBackgroundColor(new Color(245, 247, 250));
        table.addCell(cell);
    }

    private ByteArrayOutputStream buildPdf(java.util.function.Consumer<Document> builder) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();
            builder.accept(doc);
            doc.close();
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
        return baos;
    }

    // ─── Libro Diario ──────────────────────────────────────

    @Override
    public byte[] generarLibroDiario(List<AsientoResponse> asientos, LocalDate desde, LocalDate hasta) {
        return buildPdf(doc -> {
            addHeader(doc, "Libro Diario", "Desde " + desde.format(DATE_FMT) + " hasta " + hasta.format(DATE_FMT));

            PdfPTable table = createTable(new String[]{"#", "Fecha", "Descripción", "Débito", "Crédito"}, new float[]{1, 2, 6, 2, 2});
            for (AsientoResponse a : asientos) {
                addCell(table, String.valueOf(a.numeroAsiento()), cellFont(), Element.ALIGN_CENTER);
                addCell(table, a.fecha().format(DATE_FMT), cellFont(), Element.ALIGN_LEFT);
                addCell(table, a.descripcion(), cellFont(), Element.ALIGN_LEFT);
                addCell(table, MONEY.format(a.totalDebito()), cellFont(), Element.ALIGN_RIGHT);
                addCell(table, MONEY.format(a.totalCredito()), cellFont(), Element.ALIGN_RIGHT);
            }
            doc.add(table);

            BigDecimal totalD = asientos.stream().map(AsientoResponse::totalDebito).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalC = asientos.stream().map(AsientoResponse::totalCredito).reduce(BigDecimal.ZERO, BigDecimal::add);
            Paragraph resumen = new Paragraph("Total Débito: " + MONEY.format(totalD) + "  |  Total Crédito: " + MONEY.format(totalC) + "  |  Asientos: " + asientos.size(), boldFont());
            resumen.setSpacingBefore(10);
            doc.add(resumen);
        }).toByteArray();
    }

    // ─── Libro Mayor ───────────────────────────────────────

    @Override
    public byte[] generarLibroMayor(LibroMayorResponse data) {
        return buildPdf(doc -> {
            addHeader(doc, "Libro Mayor: " + data.cuentaNombre(), data.cuentaCodigo() + " — " + data.tipoCuenta());

            PdfPTable table = createTable(new String[]{"Cuenta", "Nombre", "Débito", "Crédito"}, new float[]{2, 5, 2, 2});
            for (LineaAsientoResponse m : data.movimientos()) {
                addCell(table, m.cuentaCodigo(), cellFont(), Element.ALIGN_LEFT);
                addCell(table, m.cuentaNombre(), cellFont(), Element.ALIGN_LEFT);
                addCell(table, m.esDebito() ? MONEY.format(m.monto()) : "", cellFont(), Element.ALIGN_RIGHT);
                addCell(table, !m.esDebito() ? MONEY.format(m.monto()) : "", cellFont(), Element.ALIGN_RIGHT);
            }
            doc.add(table);

            Paragraph resumen = new Paragraph("Total Débitos: " + MONEY.format(data.totalDebitos()) +
                    "  |  Total Créditos: " + MONEY.format(data.totalCreditos()) +
                    "  |  Saldo Final: " + MONEY.format(data.saldoFinal()), boldFont());
            resumen.setSpacingBefore(10);
            doc.add(resumen);
        }).toByteArray();
    }

    // ─── Estado de Resultados ─────────────────────────────

    @Override
    public byte[] generarEstadoResultados(EstadoResultadosResponse data) {
        return buildPdf(doc -> {
            addHeader(doc, "Estado de Resultados", data.fechaInicio().format(DATE_FMT) + " → " + data.fechaFin().format(DATE_FMT));

            PdfPTable table = createTable(new String[]{"Concepto", "Monto"}, new float[]{3, 2});
            addCellAlt(table, "Total Ingresos", cellFont(), Element.ALIGN_LEFT, false);
            addCellAlt(table, MONEY.format(data.totalIngresos()), cellFont(), Element.ALIGN_RIGHT, false);
            addCellAlt(table, "Total Gastos", cellFont(), Element.ALIGN_LEFT, true);
            addCellAlt(table, MONEY.format(data.totalGastos()), cellFont(), Element.ALIGN_RIGHT, true);
            addCellAlt(table, "Utilidad Neta", boldFont(), Element.ALIGN_LEFT, false);
            addCellAlt(table, MONEY.format(data.utilidadNeta()), boldFont(), Element.ALIGN_RIGHT, false);
            doc.add(table);
        }).toByteArray();
    }

    // ─── Balance General ──────────────────────────────────

    @Override
    public byte[] generarBalanceGeneral(BalanceGeneralResponse data) {
        return buildPdf(doc -> {
            addHeader(doc, "Balance General", "Al " + data.fecha().format(DATE_FMT));

            PdfPTable table = createTable(new String[]{"Concepto", "Monto"}, new float[]{3, 2});
            addCellAlt(table, "Total Activos", cellFont(), Element.ALIGN_LEFT, false);
            addCellAlt(table, MONEY.format(data.totalActivos()), cellFont(), Element.ALIGN_RIGHT, false);
            addCellAlt(table, "Total Pasivos", cellFont(), Element.ALIGN_LEFT, true);
            addCellAlt(table, MONEY.format(data.totalPasivos()), cellFont(), Element.ALIGN_RIGHT, true);
            addCellAlt(table, "Total Patrimonio", cellFont(), Element.ALIGN_LEFT, false);
            addCellAlt(table, MONEY.format(data.totalPatrimonio()), cellFont(), Element.ALIGN_RIGHT, false);
            doc.add(table);

            Paragraph verif = new Paragraph(data.cuadrado() ? "✅ Activos = Pasivos + Patrimonio" : "⚠ Balance descuadrado", boldFont());
            verif.setSpacingBefore(10);
            doc.add(verif);
        }).toByteArray();
    }

    // ─── Nómina ───────────────────────────────────────────

    @Override
    public byte[] generarNomina(List<NominaResponse> nominas) {
        return buildPdf(doc -> {
            addHeader(doc, "Nómina", nominas.size() + " registro(s)");

            PdfPTable table = createTable(new String[]{"Empleado", "Período", "Salario Base", "Deducciones", "Neto", "Estado"}, new float[]{3, 4, 2, 2, 2, 1});
            for (NominaResponse n : nominas) {
                String periodo = n.periodoInicio().format(DATE_FMT) + " → " + n.periodoFin().format(DATE_FMT);
                addCell(table, n.nombreEmpleado(), cellFont(), Element.ALIGN_LEFT);
                addCell(table, periodo, cellFont(), Element.ALIGN_LEFT);
                addCell(table, MONEY.format(n.salarioBase()), cellFont(), Element.ALIGN_RIGHT);
                addCell(table, MONEY.format(n.totalDeducciones()), cellFont(), Element.ALIGN_RIGHT);
                addCell(table, MONEY.format(n.salarioNeto()), cellFont(), Element.ALIGN_RIGHT);
                addCell(table, n.estado(), cellFont(), Element.ALIGN_CENTER);
            }
            doc.add(table);
        }).toByteArray();
    }
}
