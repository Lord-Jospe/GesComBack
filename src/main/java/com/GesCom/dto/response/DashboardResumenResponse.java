package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DashboardResumenResponse(
        BigDecimal ventas,
        BigDecimal gastos,
        BigDecimal ganancia,
        long transacciones,
        String moneda
) {}
