package com.GesCom.controller;


import com.GesCom.dto.request.CrearUsuarioRequest;
import com.GesCom.dto.request.EditarUsuarioRequest;
import com.GesCom.dto.response.UsuarioResponse;
import com.GesCom.model.Usuario;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.security.user.UsuarioDetails;
import com.GesCom.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // POST /api/users
    // Solo ADMIN puede crear usuarios internos
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.crearUsuario(request, empresaId));
    }

    //GET /api/users
    // Solo ADMIN ve los usuarios de su empresa
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(usuarioService.obtenerTodos(empresaId));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, empresaId));
    }

    //PUT /api/users/{id}
    //Editar usuario
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> editarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody EditarUsuarioRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();

        return ResponseEntity.ok(usuarioService.editarUsuario(id, request, empresaId));
    }

    // DELETE /api/usuarios/{id} — baja lógica, no física
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> desactivarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();
        usuarioService.desactivarUsuario(id, empresaId);
        return ResponseEntity.ok("Usuario desactivado exitosamente");
    }

    //PATCH /{id}/active
    //Metodo para activar al usuario
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails) {

        Long empresaId = usuarioDetails.getUsuario().getEmpresa().getEmpresaId();
        usuarioService.activarUsuario(id, empresaId);
        return ResponseEntity.ok("Usuario activado exitosamente");
    }
}
