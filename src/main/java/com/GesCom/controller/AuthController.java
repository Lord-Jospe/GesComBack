package com.GesCom.controller;

import com.GesCom.dto.request.ForgotPasswordRequest;
import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegisterRequest;
import com.GesCom.dto.request.ResetPasswordRequest;
import com.GesCom.dto.response.AuthResponse;
import com.GesCom.service.AuthService;
import com.GesCom.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    // POST /api/auth/register  — RF-01
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registro(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registrar(request));
    }

    // POST /api/auth/login  — RF-03
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/forgot-password  — RF-05
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> solicitarRecuperacion(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.solicitarRecuperacion(request.email());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, recibirás un enlace de recuperación"
        ));
    }

    // POST /api/auth/reset-password  — RF-05
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetearContrasena(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetearContrasena(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Contraseña actualizada exitosamente"
        ));
    }
}
