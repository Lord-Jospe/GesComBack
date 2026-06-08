package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record NominaResponse(
        Long nominaId,
        Long usuarioId,
        String nombreEmpleado,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        BigDecimal salarioBase,
        BigDecimal totalAsignaciones,
        BigDecimal totalDeducciones,
        BigDecimal salarioNeto,
        String estado,
        String notas,
        List<ConceptoNominaResponse> conceptos,
        LocalDateTime createdAt
) {}
