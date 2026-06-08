package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record LibroMayorResponse(
        Long cuentaId,
        String cuentaCodigo,
        String cuentaNombre,
        String tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal saldoFinal,
        List<LineaAsientoResponse> movimientos
) {}
