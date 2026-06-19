package com.GesCom.controller;

import com.GesCom.dto.response.*;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.ContabilidadService;
import com.GesCom.service.NominaService;
import com.GesCom.service.ReportePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportePdfController {

    private final ReportePdfService reportePdfService;
    private final ContabilidadService contabilidadService;
    private final NominaService nominaService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename + ".pdf")
                .body(pdf);
    }

    // GET /api/reports/journal?desde=&hasta=
    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<byte[]> pdfLibroDiario(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<AsientoResponse> asientos = contabilidadService.libroDiario(empresaId(ud), desde, hasta);
        return pdfResponse(reportePdfService.generarLibroDiario(asientos, desde, hasta), "libro-diario");
    }

    // GET /api/reports/ledger/{cuentaId}?desde=&hasta=
    @GetMapping("/ledger/{cuentaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<byte[]> pdfLibroMayor(
            @AuthenticationPrincipal UsuarioDetails ud,
            @PathVariable Long cuentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LibroMayorResponse data = contabilidadService.libroMayor(cuentaId, empresaId(ud), desde, hasta);
        return pdfResponse(reportePdfService.generarLibroMayor(data), "libro-mayor");
    }

    // GET /api/reports/income-statement?desde=&hasta=
    @GetMapping("/income-statement")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<byte[]> pdfEstadoResultados(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        EstadoResultadosResponse data = contabilidadService.estadoResultados(empresaId(ud), desde, hasta);
        return pdfResponse(reportePdfService.generarEstadoResultados(data), "estado-resultados");
    }

    // GET /api/reports/balance-sheet?fecha=
    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<byte[]> pdfBalanceGeneral(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        BalanceGeneralResponse data = contabilidadService.balanceGeneral(empresaId(ud), fecha);
        return pdfResponse(reportePdfService.generarBalanceGeneral(data), "balance-general");
    }

    // GET /api/reports/payroll
    @GetMapping("/payroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<byte[]> pdfNomina(@AuthenticationPrincipal UsuarioDetails ud) {
        List<NominaResponse> nominas = nominaService.listarPorEmpresa(empresaId(ud));
        return pdfResponse(reportePdfService.generarNomina(nominas), "nomina");
    }
}
