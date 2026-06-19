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
        upsertPlan("SEMILLA", BigDecimal.ZERO, 15, null, false, false, false);
        upsertPlan("EMPRENDEDOR", new BigDecimal("8.00"), 200, 50, false, false, false);
        upsertPlan("EMPRESA", new BigDecimal("20.00"), null, null, true, true, true);
        log.info("Planes actualizados: {}", planRepository.count());
    }

    private void upsertPlan(String nombre, BigDecimal precio, Integer maxTrans, Integer maxArch,
                            boolean inventario, boolean nomina, boolean contabilidad) {
        PlanSuscripcion plan = planRepository.findByNombre(nombre)
                .orElse(PlanSuscripcion.builder().nombre(nombre).build());
        plan.setPrecioUsd(precio);
        plan.setMaxTransaccionesMes(maxTrans);
        plan.setMaxArchivosMes(maxArch);
        plan.setTieneInventario(inventario);
        plan.setTieneNomina(nomina);
        plan.setTieneContabilidad(contabilidad);
        planRepository.save(plan);
    }
}
