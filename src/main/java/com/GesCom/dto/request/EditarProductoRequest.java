package com.GesCom.dto.request;

import com.GesCom.enums.UnidadMedida;

import java.math.BigDecimal;

public record EditarProductoRequest(
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        UnidadMedida unidadMedida,
        BigDecimal costoUnitario,
        BigDecimal precioVenta,
        BigDecimal stockMinimo,
        Boolean ventaBajoPedido
) {}
