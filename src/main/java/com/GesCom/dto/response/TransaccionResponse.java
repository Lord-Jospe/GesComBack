package com.GesCom.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TransaccionResponse(
        Long transaccionId,
        Long empresaId,
        String tipo,
        Long clienteId,
        String clienteNombre,
        Long proveedorId,
        String proveedorNombre,
        String numeroFactura,
        LocalDate fecha,
        String moneda,
        BigDecimal tasaBcvUsada,
        BigDecimal subtotal,
        BigDecimal ivaPorcentaje,
        BigDecimal ivaMonto,
        boolean igtfAplica,
        BigDecimal igtfMonto,
        BigDecimal descuentoGlobalPorcentaje,
        BigDecimal descuentoGlobalMonto,
        BigDecimal total,
        BigDecimal totalUsd,
        BigDecimal totalVes,
        String metodoPago,
        String estado,
        String motivoAnulacion,
        String notas,
        List<TransaccionLineaResponse> lineas,
        LocalDateTime createdAt
) {}
