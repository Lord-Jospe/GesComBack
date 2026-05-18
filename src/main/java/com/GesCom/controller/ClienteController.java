package com.GesCom.controller;

import com.GesCom.dto.request.CrearClienteRequest;
import com.GesCom.dto.request.EditarClienteRequest;
import com.GesCom.dto.response.ClienteResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // POST /api/customers
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ClienteResponse> crearCliente(
            @Valid @RequestBody CrearClienteRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.crearCliente(request, empresaId));
    }


    // GET /api/customers
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<ClienteResponse>> obtenerTodos(
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(clienteService.obtenerTodos(empresaId));
    }

    // GET /api/customers/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<ClienteResponse> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(clienteService.obtenerPorId(id, empresaId));
    }

    // PUT /api/customers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ClienteResponse> editarCliente(
            @PathVariable Long id,
            @Valid @RequestBody EditarClienteRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(clienteService.editarCliente(id, request, empresaId));
    }

    // DELETE /api/customers/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> desactivarCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        clienteService.desactivarCliente(id, empresaId);
        return ResponseEntity.ok("Cliente desactivado exitosamente");
    }

    //PATCH api/customers/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activarCliente(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        clienteService.activarCliente(id, empresaId);
        return ResponseEntity.ok("Cliente activado exitosamente");
    }
}
