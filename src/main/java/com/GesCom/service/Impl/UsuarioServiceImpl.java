package com.GesCom.service.Impl;


import com.GesCom.dto.request.CrearUsuarioRequest;
import com.GesCom.dto.request.EditarUsuarioRequest;
import com.GesCom.dto.request.UsuarioFiltroRequest;
import com.GesCom.dto.response.UsuarioPageResponse;
import com.GesCom.dto.response.UsuarioResponse;
import com.GesCom.model.Empresa;
import com.GesCom.model.PlanSuscripcion;
import com.GesCom.model.Rol;
import com.GesCom.model.Usuario;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.RolRepository;
import com.GesCom.repository.UsuarioRepository;
import com.GesCom.service.SuscripcionService;
import com.GesCom.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuscripcionService suscripcionService;


    // --- Crear Usuario ----
    @Override
    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request, Long empresaId) {
        // Verificar límite de usuarios del plan
        PlanSuscripcion plan = suscripcionService.obtenerPlanActivo(empresaId);
        if (plan.getMaxUsuarios() != null) {
            long actuales = usuarioRepository.findByEmpresa_EmpresaId(empresaId).stream()
                    .filter(u -> u.isActive()).count();
            if (actuales >= plan.getMaxUsuarios()) {
                throw new IllegalStateException(
                        "Límite de usuarios alcanzado (" + plan.getMaxUsuarios() + "). Actualiza tu plan.");
            }
        }

        if (usuarioRepository.existsByEmailAndIsActiveTrue(request.email())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa no encontrada"));

        Rol rol = rolRepository.findByNombre(request.rol().name())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.rol()));


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
                .sueldo(request.sueldo())
                .monedaSueldo(request.monedaSueldo())
                .build();

        var saved = usuarioRepository.save(usuario);
        log.info("Usuario creado: id={}, email={}, rol={}", saved.getUsuarioId(), request.email(), request.rol());
        return toResponse(saved);
    }


    // ---Obtener todos los usuarios ---
    @Override
    public List<UsuarioResponse> obtenerTodos(Long empresaId) {
        return usuarioRepository
                .findByEmpresa_EmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();

    }

    // --- Obtener por usuario por ID ---
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);
        return toResponse(usuario);
    }

    // --- Editar usuario ---
    @Override
    @Transactional
    public UsuarioResponse editarUsuario(Long id, EditarUsuarioRequest request, Long empresaId) {

        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);

        // Verificar email duplicado
        if (request.email() != null
                && !request.email().equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.existsByEmailAndIsActiveTrue(request.email())) {
            throw new IllegalArgumentException("El email ya está en uso: " + request.email());
        }

        if (request.primerNombre()    != null) usuario.setPrimerNombre(request.primerNombre());
        if (request.segundoNombre()   != null) usuario.setSegundoNombre(request.segundoNombre());
        if (request.primerApellido()  != null) usuario.setPrimerApellido(request.primerApellido());
        if (request.segundoApellido() != null) usuario.setSegundoApellido(request.segundoApellido());
        if (request.email()           != null) usuario.setEmail(request.email());
        if (request.rol()             != null) {
            Rol rol = rolRepository.findByNombre(request.rol().name())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.rol()));
            usuario.setRol(rol);
        }


        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.sueldo()    != null) usuario.setSueldo(request.sueldo());
        if (request.monedaSueldo() != null) usuario.setMonedaSueldo(request.monedaSueldo());

        usuario.setUpdatedAt(LocalDateTime.now());

        var saved = usuarioRepository.save(usuario);
        log.info("Usuario editado: id={}", id);
        return toResponse(saved);
    }


    // --- Desactivar usuario ---
    @Override
    @Transactional
    public void desactivarUsuario(Long id, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);

        if (!usuario.isActive()) {
            throw new IllegalStateException("El usuario ya se encuentra desactivado");
        }
        usuario.setActive(false);
        usuario.setUpdatedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
        log.info("Usuario desactivado: id={}, email={}", id, usuario.getEmail());
    }

    // --- Activar usuario
    @Transactional
    public void activarUsuario(Long id, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeLaEmpresa(id, empresaId);

        if (usuario.isActive()) {
            throw new IllegalStateException("El usuario ya se encuentra activo");
        }

        // Verificar que el email no lo tenga otro usuario activo (Opción A)
        if (usuarioRepository.existsByEmailAndIsActiveTrue(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "El email " + usuario.getEmail() + " ya está en uso por otro usuario activo. "
                    + "Cambia el email del usuario antes de reactivarlo.");
        }

        usuario.setActive(true);
        usuario.setUpdatedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
        log.info("Usuario activado: id={}, email={}", id, usuario.getEmail());
    }

    // --- Paginación y filtros
    @Override
    @Transactional(readOnly = true)
    public UsuarioPageResponse obtenerPaginado(Long empresaId, UsuarioFiltroRequest filtro) {
        var pageable = PageRequest.of(filtro.pagina(), filtro.tamano());
        var page = usuarioRepository.findPaginado(
                empresaId,
                filtro.soloActivos(),
                filtro.rolId(),
                filtro.busqueda() != null && !filtro.busqueda().isBlank() ? filtro.busqueda() : null,
                pageable);

        return new UsuarioPageResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isLast());
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
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .activo(u.isActive())
                .sueldo(u.getSueldo())
                .monedaSueldo(u.getMonedaSueldo())
                .build();
    }


}
