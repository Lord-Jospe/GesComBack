package com.GesCom.controller;

import com.GesCom.dto.request.CalcularNominaRequest;
import com.GesCom.dto.response.NominaResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.NominaService;
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
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class NominaController {

    private final NominaService nominaService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    // POST /api/payroll — RF-58
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<NominaResponse> calcular(
            @Valid @RequestBody CalcularNominaRequest req, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nominaService.calcularNomina(req, empresaId(ud)));
    }

    // GET /api/payroll — RF-63
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<NominaResponse>> listar(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(nominaService.listarPorEmpresa(empresaId(ud)));
    }

    // GET /api/payroll/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<NominaResponse> porId(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(nominaService.obtenerPorId(id, empresaId(ud)));
    }

    // GET /api/payroll/employee/{usuarioId} — RF-62
    @GetMapping("/employee/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<NominaResponse>> porEmpleado(
            @PathVariable Long usuarioId, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(nominaService.listarPorEmpleado(usuarioId, empresaId(ud)));
    }

    // PATCH /api/payroll/{id}/pay
    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> pagar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        nominaService.marcarPagada(id, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Nómina marcada como pagada"));
    }

    // PATCH /api/payroll/{id}/cancel
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> anular(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        nominaService.anular(id, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Nómina anulada"));
    }
}
