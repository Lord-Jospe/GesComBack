package com.GesCom.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CalcularNominaRequest(
        @NotNull(message = "El ID del empleado es obligatorio")
        Long usuarioId,

        @NotNull LocalDate periodoInicio,
        @NotNull LocalDate periodoFin,

        List<ConceptoExtraRequest> extras,
        String notas
) {}
