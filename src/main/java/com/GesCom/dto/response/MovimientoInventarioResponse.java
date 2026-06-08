package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MovimientoInventarioResponse(
        Long movimientoId,
        Long productoId,
        String productoNombre,
        String tipo,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        String motivo,
        Long transaccionId,
        String registradoPor,
        LocalDateTime createdAt
) {}
