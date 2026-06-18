package com.GesCom.controller;

import com.GesCom.dto.response.ConciliacionResponse;
import com.GesCom.dto.response.MovimientoBancoResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.ConciliacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
public class ConciliacionController {

    private final ConciliacionService conciliacionService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    // POST /api/reconciliation/movements — agregar movimiento manual
    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<MovimientoBancoResponse> agregarMovimiento(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestBody Map<String, Object> body) {
        LocalDate fecha = LocalDate.parse((String) body.get("fecha"));
        String descripcion = (String) body.get("descripcion");
        BigDecimal monto = new BigDecimal(body.get("monto").toString());
        String tipo = (String) body.get("tipo");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conciliacionService.agregarMovimiento(empresaId(ud), fecha, descripcion, monto, tipo));
    }

    // GET /api/reconciliation/movements
    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<MovimientoBancoResponse>> listarMovimientos(
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(conciliacionService.listarMovimientos(empresaId(ud)));
    }

    // GET /api/reconciliation?desde=&hasta= — obtener conciliación completa
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ConciliacionResponse> obtenerConciliacion(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(conciliacionService.obtenerConciliacion(empresaId(ud), desde, hasta));
    }

    // POST /api/reconciliation/auto-match — auto-conciliar en lote
    @PostMapping("/auto-match")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, Object>> autoConciliar(
            @AuthenticationPrincipal UsuarioDetails ud) {
        int count = conciliacionService.autoConciliar(empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", count + " movimiento(s) conciliado(s)", "count", count));
    }

    // POST /api/reconciliation/match/{movBancoId} — vincular a transacción
    @PostMapping("/match/{movBancoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> vincular(
            @AuthenticationPrincipal UsuarioDetails ud,
            @PathVariable Long movBancoId,
            @RequestBody Map<String, Long> body) {
        conciliacionService.vincular(movBancoId, body.get("transaccionId"), empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Conciliado exitosamente"));
    }

    // POST /api/reconciliation/unmatch/{movBancoId}
    @PostMapping("/unmatch/{movBancoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> desvincular(
            @AuthenticationPrincipal UsuarioDetails ud,
            @PathVariable Long movBancoId) {
        conciliacionService.desvincular(movBancoId, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Desvinculado exitosamente"));
    }

    // POST /api/reconciliation/reconcile/{movBancoId} — conciliar sin transacción
    @PostMapping("/reconcile/{movBancoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> conciliarSinTransaccion(
            @AuthenticationPrincipal UsuarioDetails ud,
            @PathVariable Long movBancoId) {
        conciliacionService.conciliarSinTransaccion(movBancoId, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Conciliado manualmente"));
    }

    // DELETE /api/reconciliation/movements/{movBancoId}
    @DeleteMapping("/movements/{movBancoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> eliminarMovimiento(
            @AuthenticationPrincipal UsuarioDetails ud,
            @PathVariable Long movBancoId) {
        conciliacionService.eliminarMovimiento(movBancoId, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Movimiento eliminado"));
    }

    // POST /api/reconciliation/import-csv
    @PostMapping("/import-csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> importarCSV(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestBody Map<String, String> body) {
        conciliacionService.importarCSV(empresaId(ud), body.get("csv"));
        return ResponseEntity.ok(Map.of("mensaje", "CSV importado exitosamente"));
    }
}
