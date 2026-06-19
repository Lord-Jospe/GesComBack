package com.GesCom.controller;

import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    // ─── QR ──────────────────────────────────────

    @GetMapping("/qr")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> obtenerQR() {
        try { return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(paymentService.obtenerQR()); }
        catch (IOException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/qr")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> subirQR(@RequestParam("file") MultipartFile file) {
        try { paymentService.subirQR(file); return ResponseEntity.ok(Map.of("mensaje", "QR actualizado")); }
        catch (IOException e) { return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage())); }
    }

    // ─── Comprobantes (cliente) ──────────────────

    @PostMapping("/proof")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> subirComprobante(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UsuarioDetails ud) {
        try { return ResponseEntity.ok(paymentService.subirComprobante(file, empresaId(ud))); }
        catch (IOException e) { return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/proof")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> misComprobantes(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(paymentService.misComprobantes(empresaId(ud)));
    }

    @GetMapping("/proof/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> descargarComprobante(@PathVariable Long id) {
        try {
            byte[] data = paymentService.descargarComprobante(id);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "inline").body(data);
        } catch (IOException e) { return ResponseEntity.internalServerError().build(); }
    }

    // ─── Super Admin ────────────────────────────

    @GetMapping("/admin/proofs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> comprobantesPendientes() {
        return ResponseEntity.ok(paymentService.comprobantesPendientes());
    }

    @PostMapping("/admin/proofs/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> revisarComprobante(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        paymentService.revisarComprobante(id,
                body.getOrDefault("estado", "APROBADO"),
                body.get("notas"));
        return ResponseEntity.ok(Map.of("mensaje", "Comprobante actualizado"));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(paymentService.estadisticas());
    }
}
