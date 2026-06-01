package com.GesCom.config;

import com.GesCom.model.PlanSuscripcion;
import com.GesCom.model.Rol;
import com.GesCom.repository.PlanSuscripcionRepository;
import com.GesCom.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Precarga en base de datos los roles y planes de suscripción
 * si no existen todavía. Idempotente: no inserta duplicados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PlanSuscripcionRepository planRepository;

    @Override
    public void run(String... args) {
        crearRoles();
        crearPlanes();
    }

    private void crearRoles() {
        if (rolRepository.count() == 0) {
            rolRepository.save(Rol.builder()
                    .nombre("ADMIN")
                    .descripcion("Administrador de la empresa — acceso total")
                    .build());
            rolRepository.save(Rol.builder()
                    .nombre("CONTADOR")
                    .descripcion("Contador — acceso a transacciones, contabilidad y reportes")
                    .build());
            rolRepository.save(Rol.builder()
                    .nombre("OPERADOR")
                    .descripcion("Operador de caja — acceso limitado a ventas y clientes")
                    .build());
            log.info("Roles pre-cargados: ADMIN, CONTADOR, OPERADOR");
        } else {
            log.info("Roles ya existentes: {}", rolRepository.count());
        }
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
