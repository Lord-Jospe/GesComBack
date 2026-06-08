package com.GesCom.controller;

import com.GesCom.dto.request.CrearAsientoRequest;
import com.GesCom.dto.response.*;
import com.GesCom.enums.TipoCuenta;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.ContabilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class ContabilidadController {

    private final ContabilidadService contabilidadService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    // ─── Plan de cuentas ──────────────────────────────────────────

    @GetMapping("/chart-of-accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<PlanCuentaResponse>> planCuentas(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(contabilidadService.obtenerPlanCuentas(empresaId(ud)));
    }

    @PostMapping("/chart-of-accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<PlanCuentaResponse> crearCuenta(
            @RequestBody Map<String, Object> body, @AuthenticationPrincipal UsuarioDetails ud) {
        TipoCuenta tipo = TipoCuenta.valueOf((String) body.get("tipoCuenta"));
        String codigo = (String) body.get("codigo");
        String nombre = (String) body.get("nombre");
        Long padreId = body.get("cuentaPadreId") != null
                ? ((Number) body.get("cuentaPadreId")).longValue() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contabilidadService.crearCuenta(tipo, codigo, nombre, padreId, empresaId(ud)));
    }

    @DeleteMapping("/chart-of-accounts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> desactivarCuenta(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        contabilidadService.desactivarCuenta(id, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Cuenta desactivada"));
    }

    // ─── Asientos ─────────────────────────────────────────────────

    @PostMapping("/entries")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<AsientoResponse> crearAsiento(
            @Valid @RequestBody CrearAsientoRequest req, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contabilidadService.crearAsientoManual(req, empresaId(ud)));
    }

    @GetMapping("/entries/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<AsientoResponse> obtenerAsiento(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(contabilidadService.obtenerAsiento(id, empresaId(ud)));
    }

    // ─── Libro Diario (RF-49) ─────────────────────────────────────

    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<AsientoResponse>> libroDiario(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(contabilidadService.libroDiario(empresaId(ud), desde, hasta));
    }

    // ─── Libro Mayor (RF-50) ──────────────────────────────────────

    @GetMapping("/ledger/{cuentaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<LibroMayorResponse> libroMayor(
            @PathVariable Long cuentaId, @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(contabilidadService.libroMayor(cuentaId, empresaId(ud), desde, hasta));
    }

    // ─── Estado de Resultados (RF-51) ─────────────────────────────

    @GetMapping("/income-statement")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<EstadoResultadosResponse> estadoResultados(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(contabilidadService.estadoResultados(empresaId(ud), desde, hasta));
    }

    // ─── Balance General (RF-52) ──────────────────────────────────

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<BalanceGeneralResponse> balanceGeneral(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(contabilidadService.balanceGeneral(empresaId(ud), fecha));
    }

    // ─── Cierre de período (RF-55) ────────────────────────────────

    @PostMapping("/close-period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cerrarPeriodo(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        contabilidadService.cerrarPeriodo(empresaId(ud), desde, hasta);
        return ResponseEntity.ok(Map.of("mensaje", "Período cerrado exitosamente"));
    }
}
