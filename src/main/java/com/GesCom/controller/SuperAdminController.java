package com.GesCom.controller;

import com.GesCom.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @GetMapping("/empresas")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarEmpresas() {
        return ResponseEntity.ok(superAdminService.listarEmpresas());
    }

    @PostMapping("/suscripcion/{empresaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> actualizarSuscripcion(
            @PathVariable Long empresaId,
            @RequestBody Map<String, Object> body) {
        superAdminService.actualizarSuscripcion(
                empresaId,
                body.containsKey("fechaVence") ? LocalDate.parse((String) body.get("fechaVence")) : null,
                body.containsKey("planId") ? ((Number) body.get("planId")).longValue() : null,
                body.containsKey("estado") ? (String) body.get("estado") : null
        );
        return ResponseEntity.ok(Map.of("mensaje", "Suscripción actualizada"));
    }

    @GetMapping("/comprobantes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarComprobantes() {
        return ResponseEntity.ok(superAdminService.listarComprobantes());
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(superAdminService.estadisticas());
    }
}
