package com.GesCom.service.Impl;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegisterRequest;
import com.GesCom.dto.response.AuthResponse;
import com.GesCom.exception.UsuarioInactivoException;
import com.GesCom.model.*;
import com.GesCom.repository.*;
import com.GesCom.security.jwt.JwtUtil;
import com.GesCom.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${application.super-admin.email}")
    private String superAdminEmail;
    private final UsuarioRepository        usuarioRepository;
    private final EmpresaRepository        empresaRepository;
    private final PlanSuscripcionRepository planRepository;
    private final RolRepository            rolRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PasswordEncoder          passwordEncoder;
    private final JwtUtil                  jwtUtil;
    private final AuthenticationManager   authenticationManager;

    @Override
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {

        // 1. Validar duplicados
        if (empresaRepository.existsByRif(request.rif())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese RIF");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo");
        }

        // 2. Crear empresa
        Empresa empresa = empresaRepository.save(
                Empresa.builder()
                        .nombre(request.nombreEmpresa())
                        .rif(request.rif())
                        .correo(request.email())
                        .telefono(request.telefono())
                        .monedaBase("USD")
                        .ivaActivo(true)
                        .ivaPorcentaje(new java.math.BigDecimal("16.00"))
                        .igtfActivo(false)
                        .facturaSiguienteNumero(1)
                        .isActive(true)
                        .build()
        );

        // 3. Buscar plan SEMILLA y crear suscripción
        PlanSuscripcion plan = planRepository.findByNombre("SEMILLA")
                .orElseThrow(() -> new IllegalStateException(
                        "Plan SEMILLA no encontrado. Verificar datos iniciales"));

        suscripcionRepository.save(
                Suscripcion.builder()
                        .empresa(empresa)
                        .plan(plan)
                        .fechaInicio(LocalDate.now())
                        .fechaVence(LocalDate.now().plusMonths(1))
                        .estado("ACTIVA")
                        .build()
        );

        // 4. Buscar rol ADMIN y crear usuario
        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "Rol ADMIN no encontrado. Verificar datos iniciales"));

        Usuario admin = usuarioRepository.save(
                Usuario.builder()
                        .empresa(empresa)
                        .primerNombre(request.primerNombre())
                        .segundoNombre(request.segundoNombre())
                        .primerApellido(request.primerApellido())
                        .segundoApellido(request.segundoApellido())
                        .email(request.email())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .rol(rolAdmin)
                        .isActive(true)
                        .build()
        );

        var response = buildAuthResponse(jwtUtil.generateToken(admin), admin);
        log.info("Empresa registrada: {} (RIF: {}), admin: {}", request.nombreEmpresa(), request.rif(), request.email());
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: email no encontrado — {}", request.email());
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        if (!usuario.isActive()) {
            log.warn("Intento de login: usuario inactivo — {}", request.email());
            throw new UsuarioInactivoException("Usuario desactivado. Contacte al administrador");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Asignar SUPER_ADMIN si el email coincide
        if (superAdminEmail != null && superAdminEmail.equalsIgnoreCase(request.email())
                && !usuario.getRol().getNombre().equals("SUPER_ADMIN")) {
            Rol rolSuperAdmin = rolRepository.findByNombre("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rol SUPER_ADMIN no encontrado"));
            usuario.setRol(rolSuperAdmin);
            usuarioRepository.save(usuario);
            log.info("Usuario {} ascendido a SUPER_ADMIN al iniciar sesión", request.email());
        }

        var response = buildAuthResponse(jwtUtil.generateToken(usuario), usuario);
        log.info("Login exitoso: {} (rol: {})", request.email(), usuario.getRol().getNombre());
        return response;
    }

    private AuthResponse buildAuthResponse(String token, Usuario usuario) {
        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getUsuarioId())
                .empresaId(usuario.getEmpresa().getEmpresaId())
                .nombreCompleto(usuario.getPrimerNombre() + " " + usuario.getPrimerApellido())
                .rol(usuario.getRol().getNombre())
                .nombreEmpresa(usuario.getEmpresa().getNombre())
                .build();
    }
}
