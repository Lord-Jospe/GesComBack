package com.GesCom.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TasaBcvResponse(
        Long tasaId,
        BigDecimal tasa,
        LocalDate fecha,
        String registradoPor
) {
}
