package com.GesCom.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
public record DashboardChartsResponse(
        List<VentasDiariasItem> ventas30Dias,
        List<IngresoVsGastoItem> ingresosVsGastos6Meses,
        List<CategoriaItem> categorias,
        BigDecimal porCobrar,
        BigDecimal porPagar,
        long productosCriticos,
        String monedaBase,
        BigDecimal tasaBcvActual
) {
    @Builder
    public record VentasDiariasItem(LocalDate fecha, BigDecimal monto) {}

    @Builder
    public record IngresoVsGastoItem(String mes, BigDecimal ingresos, BigDecimal gastos) {}

    @Builder
    public record CategoriaItem(String categoria, BigDecimal monto, double porcentaje) {}
}
