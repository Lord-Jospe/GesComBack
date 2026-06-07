package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AsientoResponse(
        Long asientoId,
        Integer numeroAsiento,
        LocalDate fecha,
        String descripcion,
        Long transaccionId,
        boolean esAutomatico,
        boolean periodoCerrado,
        BigDecimal totalDebito,
        BigDecimal totalCredito,
        List<LineaAsientoResponse> lineas,
        LocalDateTime createdAt
) {}
