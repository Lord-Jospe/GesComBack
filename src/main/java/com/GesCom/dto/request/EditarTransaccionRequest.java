package com.GesCom.dto.request;

import com.GesCom.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EditarTransaccionRequest(
        LocalDate fecha,
        MetodoPago metodoPago,
        BigDecimal descuentoGlobalPorcentaje,
        BigDecimal descuentoGlobalMonto,
        String notas
) {}
