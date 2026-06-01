package com.GesCom.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagoResponse(
        Long pagoId,
        Long transaccionId,
        BigDecimal monto,
        LocalDate fecha,
        String metodoPago,
        String referencia,
        String notas,
        LocalDateTime createdAt
) {}
