package com.GesCom.service;

import com.GesCom.dto.request.CrearAsientoRequest;
import com.GesCom.dto.response.*;
import com.GesCom.enums.TipoCuenta;

import java.time.LocalDate;
import java.util.List;

public interface ContabilidadService {
    // Plan de cuentas (RF-46)
    List<PlanCuentaResponse> obtenerPlanCuentas(Long empresaId);
    PlanCuentaResponse crearCuenta(TipoCuenta tipoCuenta, String codigo, String nombre, Long cuentaPadreId, Long empresaId);
    void desactivarCuenta(Long cuentaId, Long empresaId);

    // Asientos (RF-47, RF-48)
    AsientoResponse crearAsientoManual(CrearAsientoRequest request, Long empresaId);
    AsientoResponse obtenerAsiento(Long id, Long empresaId);

    // Libro Diario (RF-49)
    List<AsientoResponse> libroDiario(Long empresaId, LocalDate desde, LocalDate hasta);

    // Libro Mayor (RF-50)
    LibroMayorResponse libroMayor(Long cuentaId, Long empresaId, LocalDate desde, LocalDate hasta);

    // Estado de Resultados (RF-51)
    EstadoResultadosResponse estadoResultados(Long empresaId, LocalDate desde, LocalDate hasta);

    // Balance General (RF-52)
    BalanceGeneralResponse balanceGeneral(Long empresaId, LocalDate fecha);

    // Cierre de período (RF-55)
    void cerrarPeriodo(Long empresaId, LocalDate desde, LocalDate hasta);
}
