package com.GesCom.controller;

import com.GesCom.dto.request.CrearProductoRequest;
import com.GesCom.dto.request.EditarProductoRequest;
import com.GesCom.dto.request.RegistrarMovimientoRequest;
import com.GesCom.dto.response.MovimientoInventarioResponse;
import com.GesCom.dto.response.PageResponse;
import com.GesCom.dto.response.ProductoResponse;
import com.GesCom.enums.TipoMovimientoInventario;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.InventarioService;
import jakarta.validation.Valid;
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
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    private Long empresaId(UsuarioDetails ud) {
        return ud.getUsuario().getEmpresa().getEmpresaId();
    }

    // POST /api/inventory
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody CrearProductoRequest req, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crearProducto(req, empresaId(ud)));
    }

    // GET /api/inventory
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<ProductoResponse>> obtenerTodos(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(inventarioService.obtenerTodos(empresaId(ud)));
    }

    // GET /api/inventory/paged
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<PageResponse<ProductoResponse>> paginado(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return ResponseEntity.ok(inventarioService.obtenerPaginado(empresaId(ud), pagina, tamano));
    }

    // GET /api/inventory/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<ProductoResponse> porId(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(inventarioService.obtenerPorId(id, empresaId(ud)));
    }

    // PUT /api/inventory/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<ProductoResponse> editar(
            @PathVariable Long id, @Valid @RequestBody EditarProductoRequest req, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(inventarioService.editarProducto(id, req, empresaId(ud)));
    }

    // DELETE /api/inventory/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> desactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        inventarioService.desactivarProducto(id, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Producto desactivado"));
    }

    // PATCH /api/inventory/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, String>> activar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        inventarioService.activarProducto(id, empresaId(ud));
        return ResponseEntity.ok(Map.of("mensaje", "Producto activado"));
    }

    // GET /api/inventory/critical — RF-42, RF-43
    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<ProductoResponse>> stockCritico(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(inventarioService.stockCritico(empresaId(ud)));
    }

    // GET /api/inventory/value — RF-44
    @GetMapping("/value")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<Map<String, BigDecimal>> valorTotal(@AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(Map.of("valorTotal", inventarioService.valorTotalInventario(empresaId(ud))));
    }

    // POST /api/inventory/movements — RF-39, RF-41
    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<MovimientoInventarioResponse> movimiento(
            @Valid @RequestBody RegistrarMovimientoRequest req, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventarioService.registrarMovimiento(req, empresaId(ud), ud.getUsuario().getUsuarioId()));
    }

    // GET /api/inventory/movements/all
    @GetMapping("/movements/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<PageResponse<MovimientoInventarioResponse>> todosMovimientos(
            @AuthenticationPrincipal UsuarioDetails ud,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(required = false) TipoMovimientoInventario tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(inventarioService.todosMovimientos(empresaId(ud), pagina, tamano, tipo, desde, hasta));
    }

    // GET /api/inventory/{id}/movements
    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR', 'OPERADOR')")
    public ResponseEntity<List<MovimientoInventarioResponse>> historial(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioDetails ud) {
        return ResponseEntity.ok(inventarioService.historialMovimientos(id, empresaId(ud)));
    }
}
