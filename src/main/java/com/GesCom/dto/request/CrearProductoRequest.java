package com.GesCom.dto.request;

import com.GesCom.enums.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearProductoRequest(
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,
        String categoria,

        @NotNull(message = "La unidad de medida es obligatoria")
        UnidadMedida unidadMedida,

        BigDecimal costoUnitario,
        BigDecimal precioVenta,
        BigDecimal stockInicial,
        BigDecimal stockMinimo,
        boolean ventaBajoPedido
) {}
