package com.GesCom.controller;

import com.GesCom.dto.request.CrearTransaccionRequest;
import com.GesCom.dto.request.EditarTransaccionRequest;
import com.GesCom.dto.request.FiltroTransaccionRequest;
import com.GesCom.dto.request.RegistrarPagoRequest;
import com.GesCom.dto.response.PagoResponse;
import com.GesCom.dto.response.TransaccionResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.dto.response.AdjuntoResponse;
import com.GesCom.service.AdjuntoService;
import com.GesCom.service.FacturaPdfService;
import com.GesCom.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final FacturaPdfService facturaPdfService;
    private final AdjuntoService adjuntoService;

    // POST /api/transactions  — RF-24, RF-30
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<TransaccionResponse> crear(
            @Valid @RequestBody CrearTransaccionRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.crear(request,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions  — listar con filtros
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<TransaccionResponse>> listar(
            @AuthenticationPrincipal UsuarioDetails ud,
            FiltroTransaccionRequest filtro) {
        return ResponseEntity.ok(
                transaccionService.listar(
                        ud.getUsuario().getEmpresa().getEmpresaId(), filtro));
    }

    // GET /api/transactions/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<TransaccionResponse> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                transaccionService.obtenerPorId(id,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // PUT /api/transactions/{id}  — RF-36
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<TransaccionResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarTransaccionRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                transaccionService.editar(id, request,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // PATCH /api/transactions/{id}/cancel  — RF-36
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> anular(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UsuarioDetails ud) {
        String motivo = body.getOrDefault("motivo", "Sin motivo especificado");
        transaccionService.anular(id, motivo,
                ud.getUsuario().getEmpresa().getEmpresaId());
        return ResponseEntity.ok(Map.of("mensaje", "Transacción anulada exitosamente"));
    }

    // GET /api/transactions/receivable  — RF-32
    @GetMapping("/receivable")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<TransaccionResponse>> cuentasPorCobrar(
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                transaccionService.cuentasPorCobrar(
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions/{id}/invoice  — RF-29
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<byte[]> descargarFactura(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {
        byte[] pdf = facturaPdfService.generarFactura(id,
                ud.getUsuario().getEmpresa().getEmpresaId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "factura-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // POST /api/transactions/{id}/payments  — RF-32, RF-33
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<PagoResponse> registrarPago(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarPagoRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.registrarPago(id, request,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions/{id}/payments
    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<PagoResponse>> historialPagos(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                transaccionService.historialPagos(id,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // POST /api/transactions/{id}/attachments  — RF-31
    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<AdjuntoResponse> subirAdjunto(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile archivo,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adjuntoService.subir(id, archivo,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions/{id}/attachments  — RF-31
    @GetMapping("/{id}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<AdjuntoResponse>> listarAdjuntos(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                adjuntoService.listar(id, ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions/attachments/{adjuntoId}
    @GetMapping("/attachments/{adjuntoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<byte[]> descargarAdjunto(
            @PathVariable Long adjuntoId,
            @AuthenticationPrincipal UsuarioDetails ud) {
        byte[] archivo = adjuntoService.descargar(adjuntoId,
                ud.getUsuario().getEmpresa().getEmpresaId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }

    // DELETE /api/transactions/attachments/{adjuntoId}
    @DeleteMapping("/attachments/{adjuntoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> eliminarAdjunto(
            @PathVariable Long adjuntoId,
            @AuthenticationPrincipal UsuarioDetails ud) {
        adjuntoService.eliminar(adjuntoId, ud.getUsuario().getEmpresa().getEmpresaId());
        return ResponseEntity.ok(Map.of("mensaje", "Archivo eliminado exitosamente"));
    }

    // POST /api/transactions/{id}/credit-note  — RF-35
    @PostMapping("/{id}/credit-note")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<TransaccionResponse> emitirNotaCredito(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UsuarioDetails ud) {
        String motivo = body.getOrDefault("motivo", "Sin motivo especificado");
        BigDecimal monto = new java.math.BigDecimal(body.getOrDefault("monto", "0"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.emitirNotaCredito(id, motivo, monto,
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/transactions/payable  — RF-33
    @GetMapping("/payable")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<TransaccionResponse>> cuentasPorPagar(
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                transaccionService.cuentasPorPagar(
                        ud.getUsuario().getEmpresa().getEmpresaId()));
    }
}
