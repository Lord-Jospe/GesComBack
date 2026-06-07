package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ConceptoNominaResponse(
        Long conceptoId,
        String tipo,
        String descripcion,
        BigDecimal monto
) {}
