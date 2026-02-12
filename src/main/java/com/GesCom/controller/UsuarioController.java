package com.GesCom.controller;


import com.GesCom.model.Usuario;
import com.GesCom.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/demo")
    public String welcome() {
        return "Welcome";
    }

    @GetMapping("/getAllUsuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
}
