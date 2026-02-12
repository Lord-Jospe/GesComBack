package com.GesCom.security.auth;

import com.GesCom.dto.request.LoginRequest;
import com.GesCom.dto.request.RegisterRequest;
import com.GesCom.dto.response.AuthResponse;
import com.GesCom.model.Rol;
import com.GesCom.model.Usuario;
import com.GesCom.repository.RolRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {

        if (usuarioRepository.existsByEmail(registerRequest.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Rol rol = rolRepository.findByRol(registerRequest.rol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + registerRequest.rol()));

        Usuario usuario = Usuario.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .primerNombre(registerRequest.primerNombre())
                .segundoNombre(registerRequest.segundoNombre())
                .primerApellido(registerRequest.primerApellido())
                .segundoApellido(registerRequest.segundoApellido())
                .sexo(registerRequest.sexo())
                .rol(rol)
                .isActive(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        String token = jwtUtil.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

}
