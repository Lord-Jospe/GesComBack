package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SuscripcionResponse(
        Long suscripcionId,
        String planNombre,
        BigDecimal precioUsd,
        LocalDate fechaInicio,
        LocalDate fechaVence,
        String estado,
        int maxTransaccionesMes,
        int maxArchivosMes,
        boolean tieneInventario,
        boolean tieneNomina,
        boolean tieneContabilidad
) {}
