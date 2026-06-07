package com.GesCom.dto.request;

import com.GesCom.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarMovimientoRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimientoInventario tipo,

        @NotNull @Positive(message = "La cantidad debe ser positiva")
        BigDecimal cantidad,

        BigDecimal costoUnitario,
        String motivo
) {}
