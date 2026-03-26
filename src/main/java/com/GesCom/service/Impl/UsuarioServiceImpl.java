package com.GesCom.service.Impl;


import com.GesCom.dto.request.CrearUsuarioRequest;
import com.GesCom.dto.response.UsuarioResponse;
import com.GesCom.model.Empresa;
import com.GesCom.model.Rol;
import com.GesCom.model.Usuario;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.RolRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request, Long empresaId) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa no encontrada"));

        Rol rol = rolRepository.findByNombre(request.rolNombre())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rol no válido: " + request.rolNombre()));

        Usuario usuario = Usuario.builder()
                .empresa(empresa)
                .primerNombre(request.primerNombre())
                .segundoNombre(request.segundoNombre())
                .primerApellido(request.primerApellido())
                .segundoApellido(request.segundoApellido())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(rol)
                .isActive(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public List<UsuarioResponse> obtenerTodos(Long empresaId) {
        return usuarioRepository
                .findByEmpresa_EmpresaIdAndIsActiveTrue(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();

    }

    @Override
    @Transactional
    public UsuarioResponse editarUsuario(Long id, CrearUsuarioRequest request, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);

        Rol rol = rolRepository.findByNombre(request.rolNombre())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rol no válido: " + request.rolNombre()));

        usuario.setPrimerNombre(request.primerNombre());
        usuario.setSegundoNombre(request.segundoNombre());
        usuario.setPrimerApellido(request.primerApellido());
        usuario.setSegundoApellido(request.segundoApellido());
        usuario.setRol(rol);

        // Solo actualiza la contraseña si viene en el request
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void desactivarUsuario(Long id, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);
        usuario.setActive(false);
        usuarioRepository.save(usuario);
    }


    // Verifica que el usuario pertenezca a la empresa
    // del admin que ejecuta la operación — evita que
    // un admin toque usuarios de otra empresa
    private Usuario obtenerUsuarioDeLaEmpresa(Long usuarioId, Long empresaId) {
        return usuarioRepository.findById(usuarioId)
                .filter(u -> u.getEmpresa().getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado en esta empresa"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .usuarioId(u.getUsuarioId())
                .primerNombre(u.getPrimerNombre())
                .segundoNombre(u.getSegundoNombre())
                .primerApellido(u.getPrimerApellido())
                .segundoApellido(u.getSegundoApellido())
                .email(u.getEmail())
                .rol(u.getRol().getNombre())
                .isActive(u.isActive())
                .build();
    }
}
