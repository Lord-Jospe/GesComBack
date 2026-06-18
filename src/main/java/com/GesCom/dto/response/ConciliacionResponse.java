package com.GesCom.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ConciliacionResponse(
        List<MovimientoBancoResponse> conciliados,
        List<MovimientoBancoResponse> sinConciliarBanco,
        List<ConciliacionResponse.TxConciliar> sinConciliarGesCom
) {
    @Builder
    public record TxConciliar(
            Long transaccionId,
            String tipo,
            String clienteProveedor,
            String numeroFactura,
            java.time.LocalDate fecha,
            String moneda,
            java.math.BigDecimal total,
            String estado,
            String metodoPago
    ) {}
}
