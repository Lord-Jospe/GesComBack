package com.GesCom.dto.response;

import java.math.BigDecimal;

public record TransaccionLineaResponse(
        Long lineaId,
        Long productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal descuentoPorcentaje,
        BigDecimal descuentoMonto,
        BigDecimal subtotalLinea
) {}
