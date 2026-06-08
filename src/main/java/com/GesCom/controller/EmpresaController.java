package com.GesCom.controller;

import com.GesCom.dto.request.EditarEmpresaRequest;
import com.GesCom.dto.request.MonedaRequest;
import com.GesCom.dto.response.EmpresaResponse;
import com.GesCom.dto.response.SuscripcionResponse;
import com.GesCom.model.Suscripcion;
import com.GesCom.repository.SuscripcionRepository;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final SuscripcionRepository suscripcionRepository;

    // GET /api/company
    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponse> obtenerPerfil(
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(empresaService.obtenerPerfil(empresaId));
    }

    // PUT /api/company
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponse> editarPerfil(
            @Valid @RequestBody EditarEmpresaRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(empresaService.editarPerfil(empresaId, request));
    }

    // GET /api/company/subscription
    @GetMapping("/subscription")
    public ResponseEntity<SuscripcionResponse> obtenerSuscripcion(
            @AuthenticationPrincipal UsuarioDetails ud) {
        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        Suscripcion s = suscripcionRepository
                .findByEmpresa_EmpresaIdAndEstado(empresaId, "ACTIVA")
                .orElse(null);

        if (s == null) return ResponseEntity.noContent().build();

        var plan = s.getPlan();
        return ResponseEntity.ok(SuscripcionResponse.builder()
                .suscripcionId(s.getSuscripcionId())
                .planNombre(plan.getNombre())
                .precioUsd(plan.getPrecioUsd())
                .fechaInicio(s.getFechaInicio())
                .fechaVence(s.getFechaVence())
                .estado(s.getEstado())
                .maxTransaccionesMes(plan.getMaxTransaccionesMes() != null ? plan.getMaxTransaccionesMes() : 0)
                .maxArchivosMes(plan.getMaxArchivosMes() != null ? plan.getMaxArchivosMes() : 0)
                .tieneInventario(plan.isTieneInventario())
                .tieneNomina(plan.isTieneNomina())
                .tieneContabilidad(plan.isTieneContabilidad())
                .build());
    }

    // PATCH /api/company/money
    @PatchMapping("/money")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarMoneda(
            @Valid @RequestBody MonedaRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        empresaService.cambiarMoneda(ud.getUsuario().getEmpresa().getEmpresaId(), request);
        return ResponseEntity.noContent().build();
    }
}
