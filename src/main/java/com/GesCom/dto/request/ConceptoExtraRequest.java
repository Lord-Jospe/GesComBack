package com.GesCom.dto.request;

import com.GesCom.enums.TipoConcepto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConceptoExtraRequest(
        @NotNull TipoConcepto tipo,
        @NotBlank String descripcion,
        @NotNull BigDecimal monto
) {}
