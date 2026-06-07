package com.GesCom.config;

import com.GesCom.enums.TipoCuenta;
import com.GesCom.model.Empresa;
import com.GesCom.model.PlanCuenta;
import com.GesCom.repository.EmpresaRepository;
import com.GesCom.repository.PlanCuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanCuentasSeeder implements CommandLineRunner {

    private final PlanCuentaRepository planCuentaRepository;
    private final EmpresaRepository empresaRepository;

    private record CuentaSeed(String codigo, String nombre, TipoCuenta tipo, String padreCodigo) {}

    private static final List<CuentaSeed> CUENTAS_VEN_NIF = List.of(
            // ACTIVOS
            new CuentaSeed("1",     "Activos",                       TipoCuenta.ACTIVO,     null),
            new CuentaSeed("1.1",   "Activos Corrientes",            TipoCuenta.ACTIVO,     "1"),
            new CuentaSeed("1.1.1", "Efectivo y Equivalentes",       TipoCuenta.ACTIVO,     "1.1"),
            new CuentaSeed("1.1.2", "Cuentas por Cobrar",            TipoCuenta.ACTIVO,     "1.1"),
            new CuentaSeed("1.1.3", "Inventario",                    TipoCuenta.ACTIVO,     "1.1"),
            new CuentaSeed("1.2",   "Activos No Corrientes",         TipoCuenta.ACTIVO,     "1"),
            new CuentaSeed("1.2.1", "Propiedad, Planta y Equipo",    TipoCuenta.ACTIVO,     "1.2"),
            new CuentaSeed("1.2.2", "Depreciación Acumulada",        TipoCuenta.ACTIVO,     "1.2"),
            // PASIVOS
            new CuentaSeed("2",     "Pasivos",                       TipoCuenta.PASIVO,     null),
            new CuentaSeed("2.1",   "Pasivos Corrientes",            TipoCuenta.PASIVO,     "2"),
            new CuentaSeed("2.1.1", "Cuentas por Pagar",             TipoCuenta.PASIVO,     "2.1"),
            new CuentaSeed("2.1.2", "Impuestos por Pagar",           TipoCuenta.PASIVO,     "2.1"),
            new CuentaSeed("2.1.3", "Préstamos a Corto Plazo",       TipoCuenta.PASIVO,     "2.1"),
            new CuentaSeed("2.2",   "Pasivos No Corrientes",         TipoCuenta.PASIVO,     "2"),
            new CuentaSeed("2.2.1", "Préstamos a Largo Plazo",       TipoCuenta.PASIVO,     "2.2"),
            // PATRIMONIO
            new CuentaSeed("3",     "Patrimonio",                    TipoCuenta.PATRIMONIO, null),
            new CuentaSeed("3.1",   "Capital Social",                TipoCuenta.PATRIMONIO, "3"),
            new CuentaSeed("3.2",   "Utilidades Retenidas",          TipoCuenta.PATRIMONIO, "3"),
            new CuentaSeed("3.3",   "Utilidad del Ejercicio",        TipoCuenta.PATRIMONIO, "3"),
            // INGRESOS
            new CuentaSeed("4",     "Ingresos",                      TipoCuenta.INGRESO,    null),
            new CuentaSeed("4.1",   "Ventas",                        TipoCuenta.INGRESO,    "4"),
            new CuentaSeed("4.2",   "Ingresos por Servicios",        TipoCuenta.INGRESO,    "4"),
            new CuentaSeed("4.3",   "Ingresos Financieros",          TipoCuenta.INGRESO,    "4"),
            // GASTOS
            new CuentaSeed("5",     "Gastos",                        TipoCuenta.GASTO,      null),
            new CuentaSeed("5.1",   "Costo de Ventas",               TipoCuenta.GASTO,      "5"),
            new CuentaSeed("5.2",   "Gastos de Personal",            TipoCuenta.GASTO,      "5"),
            new CuentaSeed("5.3",   "Gastos Administrativos",        TipoCuenta.GASTO,      "5"),
            new CuentaSeed("5.4",   "Gastos Financieros",            TipoCuenta.GASTO,      "5"),
            new CuentaSeed("5.5",   "Depreciación",                  TipoCuenta.GASTO,      "5")
    );

    @Override
    public void run(String... args) {
        List<Empresa> empresas = empresaRepository.findAll();
        for (Empresa empresa : empresas) {
            if (planCuentaRepository.findByEmpresa_EmpresaId(empresa.getEmpresaId()).isEmpty()) {
                log.info("Sembrando plan de cuentas VEN-NIF para empresa: {}", empresa.getNombre());
                for (var seed : CUENTAS_VEN_NIF) {
                    Long padreId = null;
                    if (seed.padreCodigo() != null) {
                        padreId = planCuentaRepository
                                .findByEmpresa_EmpresaIdAndCodigo(empresa.getEmpresaId(), seed.padreCodigo())
                                .map(PlanCuenta::getCuentaId).orElse(null);
                    }
                    planCuentaRepository.save(PlanCuenta.builder()
                            .empresa(empresa).codigo(seed.codigo()).nombre(seed.nombre())
                            .tipoCuenta(seed.tipo()).cuentaPadreId(padreId)
                            .isActive(true).esPredeterminada(true).build());
                }
                log.info("Plan de cuentas sembrado: {} cuentas", CUENTAS_VEN_NIF.size());
            }
        }
    }
}
