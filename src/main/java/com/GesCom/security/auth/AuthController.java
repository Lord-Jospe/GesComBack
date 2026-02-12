package com.GesCom.security.auth;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegisterRequest;
import com.GesCom.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        final AuthResponse response = service.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        final AuthResponse response = service.register(registerRequest);
        return ResponseEntity.ok(response);
    }

}
