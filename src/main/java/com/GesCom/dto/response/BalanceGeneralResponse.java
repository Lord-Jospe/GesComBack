package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record BalanceGeneralResponse(
        LocalDate fecha,
        BigDecimal totalActivos,
        BigDecimal totalPasivos,
        BigDecimal totalPatrimonio,
        boolean cuadrado
) {}
