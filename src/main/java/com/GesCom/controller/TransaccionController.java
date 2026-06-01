package com.GesCom.controller;

import com.GesCom.dto.request.CrearTransaccionRequest;
import com.GesCom.dto.request.EditarTransaccionRequest;
import com.GesCom.dto.request.FiltroTransaccionRequest;
import com.GesCom.dto.response.TransaccionResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
