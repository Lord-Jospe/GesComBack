package com.GesCom.controller;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegistroEmpresaRequest;
import com.GesCom.dto.response.AuthResponse;
import com.GesCom.service.AuthService;
import com.GesCom.service.Impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registro(
            @Valid @RequestBody RegistroEmpresaRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registrar(request));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
