package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LineaAsientoResponse(
        Long lineaId,
        Long cuentaId,
        String cuentaCodigo,
        String cuentaNombre,
        boolean esDebito,
        BigDecimal monto
) {}
