package com.GesCom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CrearAsientoRequest(
        @NotNull LocalDate fecha,
        @NotBlank String descripcion,
        @NotNull List<LineaAsientoItem> lineas
) {
    public record LineaAsientoItem(
            @NotNull Long cuentaId,
            @NotNull Boolean esDebito,
            @NotNull java.math.BigDecimal monto
    ) {}
}
