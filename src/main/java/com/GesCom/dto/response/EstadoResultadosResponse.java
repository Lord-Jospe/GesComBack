package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record EstadoResultadosResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal totalIngresos,
        BigDecimal totalGastos,
        BigDecimal utilidadNeta
) {}
