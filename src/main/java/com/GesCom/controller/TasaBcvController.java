package com.GesCom.controller;

import com.GesCom.dto.request.TasaBcvRequest;
import com.GesCom.dto.response.TasaBcvResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.TasaBcvService;
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

@RestController
@RequestMapping("/exchange-rate")
@RequiredArgsConstructor
public class TasaBcvController {

    private final TasaBcvService tasaBcvService;

    // POST /api/exchange-rate  — RF-11
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<TasaBcvResponse> registrar(
            @Valid @RequestBody TasaBcvRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tasaBcvService.registrarTasa(
                        request,
                        ud.getUsuario().getEmpresa().getEmpresaId(),
                        ud.getUsuario().getUsuarioId()));
    }

    // GET /api/exchange-rate  — RF-12
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<TasaBcvResponse>> historial(
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                tasaBcvService.historialTasas(ud.getUsuario().getEmpresa().getEmpresaId()));
    }

    // GET /api/exchange-rate/latest  — tasa más reciente
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<TasaBcvResponse> ultimaTasa(
            @AuthenticationPrincipal UsuarioDetails ud) {
        var tasas = tasaBcvService.historialTasas(ud.getUsuario().getEmpresa().getEmpresaId());
        if (tasas.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(tasas.get(0));
    }

    // GET /api/exchange-rate/{fecha}  — RF-12
    @GetMapping("/{fecha}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<TasaBcvResponse> obtenerPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(
                tasaBcvService.obtenerTasaDelDia(ud.getUsuario().getEmpresa().getEmpresaId(), fecha));
    }
}
