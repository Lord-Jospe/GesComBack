package com.GesCom.service;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegisterRequest;
import com.GesCom.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registrar(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
