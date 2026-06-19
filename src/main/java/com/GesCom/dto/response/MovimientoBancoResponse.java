package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record MovimientoBancoResponse(
        Long movimientoBancoId,
        LocalDate fecha,
        String descripcion,
        BigDecimal monto,
        String tipo,
        Long transaccionId,
        String numeroFactura,
        boolean conciliado,
        LocalDate fechaConciliacion,
        LocalDateTime createdAt
) {}
