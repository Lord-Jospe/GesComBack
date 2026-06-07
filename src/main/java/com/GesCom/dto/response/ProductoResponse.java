package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductoResponse(
        Long productoId,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        String unidadMedida,
        BigDecimal costoUnitario,
        BigDecimal precioVenta,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        boolean ventaBajoPedido,
        String alertaStock,
        BigDecimal valorTotal,
        boolean activo,
        LocalDateTime createdAt
) {}
