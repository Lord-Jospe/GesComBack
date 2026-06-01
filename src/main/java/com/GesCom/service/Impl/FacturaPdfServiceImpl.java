package com.GesCom.service.Impl;

import com.GesCom.enums.TipoTransaccion;
import com.GesCom.model.Transaccion;
import com.GesCom.model.TransaccionLinea;
import com.GesCom.repository.TransaccionRepository;
import com.GesCom.service.FacturaPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaPdfServiceImpl implements FacturaPdfService {

    private final TransaccionRepository transaccionRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat USD_FMT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DecimalFormat DEC_FMT = new DecimalFormat("#,##0.00");

    static {
        USD_FMT.setMaximumFractionDigits(2);
    }

    @Override
    public byte[] generarFactura(Long transaccionId, Long empresaId) {
        Transaccion t = transaccionRepository
                .findByTransaccionIdAndEmpresa_EmpresaId(transaccionId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

        if (t.getTipo() != TipoTransaccion.INGRESO) {
            throw new IllegalArgumentException("Solo se puede generar factura para transacciones de INGRESO");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            var empresa = t.getEmpresa();
            var cliente = t.getCliente();

            // ─── Título ─────────────────────────────────────────────
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph titulo = new Paragraph("FACTURA", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            // ─── Datos del emisor ──────────────────────────────────
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("EMISOR", fontBold));
            document.add(new Paragraph(empresa.getNombre() +
                    " | RIF: " + empresa.getRif(), fontNormal));
            if (empresa.getDireccion() != null) {
                document.add(new Paragraph("Dirección: " + empresa.getDireccion(), fontNormal));
            }
            if (empresa.getTelefono() != null) {
                document.add(new Paragraph("Teléfono: " + empresa.getTelefono(), fontNormal));
            }
            document.add(new Paragraph(" "));

            // ─── Número y fecha ────────────────────────────────────
            document.add(new Paragraph(
                    "N°: " + t.getNumeroFactura() +
                    "  |  Fecha: " + t.getFecha().format(DATE_FMT), fontBold));
            document.add(new Paragraph(" "));

            // ─── Datos del cliente ─────────────────────────────────
            if (cliente != null) {
                document.add(new Paragraph("CLIENTE", fontBold));
                document.add(new Paragraph(cliente.getNombre() +
                        " | RIF/C.I.: " + cliente.getRifCedula(), fontNormal));
                if (cliente.getDireccion() != null) {
                    document.add(new Paragraph("Dirección: " + cliente.getDireccion(), fontNormal));
                }
                document.add(new Paragraph(" "));
            }

            // ─── Tabla de líneas ───────────────────────────────────
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            float[] cols = {3f, 1f, 1.5f, 1f, 1.5f};
            table.setWidths(cols);

            // Encabezados
            String[] headers = {"Descripción", "Cant.", "Precio", "Desc.", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (TransaccionLinea l : t.getLineas()) {
                table.addCell(new Phrase(l.getDescripcion(), fontNormal));

                PdfPCell cant = new PdfPCell(new Phrase(l.getCantidad().toString(), fontNormal));
                cant.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cant);

                PdfPCell precio = new PdfPCell(new Phrase(
                        "$" + DEC_FMT.format(l.getPrecioUnitario()), fontNormal));
                precio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(precio);

                BigDecimal desc = l.getDescuentoMonto() != null
                        ? l.getDescuentoMonto() : BigDecimal.ZERO;
                PdfPCell descCell = new PdfPCell(new Phrase(
                        desc.compareTo(BigDecimal.ZERO) > 0
                                ? "$" + DEC_FMT.format(desc) : "-", fontNormal));
                descCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(descCell);

                PdfPCell sub = new PdfPCell(new Phrase(
                        "$" + DEC_FMT.format(l.getSubtotalLinea()), fontNormal));
                sub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(sub);
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // ─── Totales ───────────────────────────────────────────
            PdfPTable totales = new PdfPTable(2);
            totales.setWidthPercentage(100);
            totales.setWidths(new float[]{3f, 1.5f});
            totales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            agregarFila(totales, "Subtotal", t.getSubtotal(), fontNormal, fontBold);
            if (t.getDescuentoGlobalMonto() != null
                    && t.getDescuentoGlobalMonto().compareTo(BigDecimal.ZERO) > 0) {
                agregarFila(totales, "Descuento Global",
                        t.getDescuentoGlobalMonto().negate(), fontNormal, fontBold);
            }
            if (t.getIvaMonto() != null && t.getIvaMonto().compareTo(BigDecimal.ZERO) > 0) {
                agregarFila(totales, "IVA (" + t.getIvaPorcentaje() + "%)",
                        t.getIvaMonto(), fontNormal, fontBold);
            }
            if (t.isIgtfAplica()) {
                agregarFila(totales, "IGTF (3%)", t.getIgtfMonto(), fontNormal, fontBold);
            }

            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            agregarFila(totales, "TOTAL " + t.getMoneda(), t.getTotal(), fontTotal, fontTotal);

            document.add(totales);
            document.add(new Paragraph(" "));

            // ─── Equivalentes en otra moneda ───────────────────────
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 9);
            document.add(new Paragraph(
                    "Tasa BCV: Bs. " + DEC_FMT.format(t.getTasaBcvUsada()) + " / USD", fontSmall));
            if (t.getTotalUsd() != null && t.getTotalVes() != null) {
                document.add(new Paragraph(
                        "Equivalente USD: $" + DEC_FMT.format(t.getTotalUsd()) +
                        "  |  Equivalente Bs.: " + DEC_FMT.format(t.getTotalVes()), fontSmall));
            }
            document.add(new Paragraph(" "));

            // ─── Método de pago ────────────────────────────────────
            document.add(new Paragraph("Método de pago: " + t.getMetodoPago(), fontNormal));
            document.add(new Paragraph("Estado: " + t.getEstado(), fontNormal));

            if (t.getNotas() != null && !t.getNotas().isBlank()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Notas: " + t.getNotas(), fontSmall));
            }

            document.close();

            log.info("Factura PDF generada: {} — {} KB",
                    t.getNumeroFactura(), baos.size() / 1024);

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar factura PDF", e);
            throw new RuntimeException("Error al generar la factura PDF: " + e.getMessage(), e);
        }
    }

    private void agregarFila(PdfPTable table, String label, BigDecimal monto,
                              Font fontLabel, Font fontMonto) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fontLabel));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lbl.setPadding(3);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(
                "$ " + DEC_FMT.format(monto), fontMonto));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setPadding(3);
        table.addCell(val);
    }
}
