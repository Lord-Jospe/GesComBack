package com.GesCom.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TasaBcvResponse(
        Long tasaId,
        BigDecimal tasa,
        LocalDateTime fechaHora,
        String registradoPor
) {
}
