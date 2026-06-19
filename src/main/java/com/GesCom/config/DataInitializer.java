package com.GesCom.config;

import com.GesCom.enums.NombreRol;
import com.GesCom.model.PlanSuscripcion;
import com.GesCom.model.Rol;
import com.GesCom.model.Usuario;
import com.GesCom.repository.PlanSuscripcionRepository;
import com.GesCom.repository.RolRepository;
import com.GesCom.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PlanSuscripcionRepository planRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${application.super-admin.email}")
    private String superAdminEmail;

    @Override
    public void run(String... args) {
        crearRoles();
        crearPlanes();
        asignarSuperAdmin();
    }

    private void crearRoles() {
        for (NombreRol nr : NombreRol.values()) {
            if (rolRepository.findByNombre(nr.name()).isEmpty()) {
                String desc = switch (nr) {
                    case SUPER_ADMIN -> "Super administrador — gestiona todas las empresas y suscripciones";
                    case ADMIN -> "Administrador de la empresa — acceso total";
                    case CONTADOR -> "Contador — acceso a transacciones, contabilidad y reportes";
                    case OPERADOR -> "Operador de caja — acceso limitado a ventas y clientes";
                };
                rolRepository.save(Rol.builder().nombre(nr.name()).descripcion(desc).build());
                log.info("Rol creado: {}", nr.name());
            }
        }
        log.info("Roles existentes: {}", rolRepository.count());
    }

    private void asignarSuperAdmin() {
        usuarioRepository.findByEmail(superAdminEmail).ifPresentOrElse(user -> {
            Rol rolSuperAdmin = rolRepository.findByNombre("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rol SUPER_ADMIN no encontrado"));
            if (!user.getRol().getNombre().equals("SUPER_ADMIN")) {
                user.setRol(rolSuperAdmin);
                usuarioRepository.save(user);
                log.info("Usuario {} asignado como SUPER_ADMIN", superAdminEmail);
            }
        }, () -> log.debug("Usuario super admin {} aún no registrado", superAdminEmail));
    }

    private void crearPlanes() {
        if (planRepository.count() == 0) {
            planRepository.save(PlanSuscripcion.builder()
                    .nombre("SEMILLA")
                    .precioUsd(BigDecimal.ZERO)
                    .maxTransaccionesMes(15)
                    .maxArchivosMes(null) // sin acceso a bóveda
                    .tieneInventario(false)
                    .tieneNomina(false)
                    .tieneContabilidad(false)
                    .build());

            planRepository.save(PlanSuscripcion.builder()
                    .nombre("EMPRENDEDOR")
                    .precioUsd(new BigDecimal("7.99"))
                    .maxTransaccionesMes(null) // ilimitado
                    .maxArchivosMes(50)
                    .tieneInventario(false)
                    .tieneNomina(false)
                    .tieneContabilidad(false)
                    .build());

            planRepository.save(PlanSuscripcion.builder()
                    .nombre("NEGOCIO")
                    .precioUsd(new BigDecimal("14.99"))
                    .maxTransaccionesMes(null) // ilimitado
                    .maxArchivosMes(null) // ilimitado
                    .tieneInventario(true)
                    .tieneNomina(true)
                    .tieneContabilidad(true)
                    .build());

            log.info("Planes pre-cargados: SEMILLA, EMPRENDEDOR, NEGOCIO");
        } else {
            log.info("Planes ya existentes: {}", planRepository.count());
        }
    }
}
