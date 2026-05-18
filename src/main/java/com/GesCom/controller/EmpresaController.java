package com.GesCom.controller;

import com.GesCom.dto.request.EditarEmpresaRequest;
import com.GesCom.dto.request.MonedaRequest;
import com.GesCom.dto.response.EmpresaResponse;
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

    // GET /api/company
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
