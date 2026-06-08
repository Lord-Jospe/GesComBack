package com.GesCom.controller;

import com.GesCom.dto.request.CrearProveedorRequest;
import com.GesCom.dto.request.EditarProveedorRequest;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProveedorResponse;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/provider")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    // POST api/provider
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ProveedorResponse> crearProveedor(
            @Valid @RequestBody CrearProveedorRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proveedorService.crearProveedor(request, empresaId));
    }

    //GET api/provider
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<ProveedorResponse>> obtenerTodos(
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(proveedorService.obtenerTodos(empresaId));
    }

    // GET /api/provider/paged?pagina=0&tamano=10
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<PageResponse<ProveedorResponse>> obtenerPaginado(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(proveedorService.obtenerPaginado(empresaId, pagina, tamano));
    }

    //GET api/provider/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<ProveedorResponse> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(proveedorService.obtenerPorId(id, empresaId));
    }

    //PUT api/provider/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ProveedorResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarProveedorRequest request,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(proveedorService.editarProveedor(id, request, empresaId));
    }

    //DELETE api/provider/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        proveedorService.desactivarProveedor(id, empresaId);
        return ResponseEntity.ok("Proveedor desactivado exitosamente");
    }

    //PATCH api/provider/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails ud) {

        Long empresaId = ud.getUsuario().getEmpresa().getEmpresaId();
        proveedorService.activarProveedor(id, empresaId);
        return ResponseEntity.ok("Proveedor activado exitosamente");
    }
}
