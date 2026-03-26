package com.GesCom.service;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegistroEmpresaRequest;
import com.GesCom.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registrar(RegistroEmpresaRequest request);
    AuthResponse login(LoginRequest request);
}
