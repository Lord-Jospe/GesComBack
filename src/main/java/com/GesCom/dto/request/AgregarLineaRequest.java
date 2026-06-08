package com.GesCom.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AgregarLineaRequest(
        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        BigDecimal descuentoPorcentaje,
        BigDecimal descuentoMonto
) {}
