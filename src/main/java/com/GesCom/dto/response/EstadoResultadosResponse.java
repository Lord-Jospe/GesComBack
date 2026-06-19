package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record EstadoResultadosResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal totalIngresos,
        BigDecimal totalGastos,
        BigDecimal utilidadNeta,
        List<DetalleItem> detalle
) {
    @Builder
    public record DetalleItem(
            String cuentaCodigo,
            String cuentaNombre,
            String tipo, // INGRESO o GASTO
            BigDecimal monto
    ) {}
}
