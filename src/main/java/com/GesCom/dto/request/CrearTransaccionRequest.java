package com.GesCom.dto.request;

import com.GesCom.enums.MetodoPago;
import com.GesCom.enums.TipoTransaccion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearTransaccionRequest(
        @NotNull(message = "El tipo de transacción es obligatorio")
        TipoTransaccion tipo,

        Long clienteId,
        Long proveedorId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "La moneda es obligatoria")
        String moneda,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        BigDecimal descuentoGlobalPorcentaje,
        BigDecimal descuentoGlobalMonto,

        String notas,

        @NotEmpty(message = "Debe incluir al menos una línea")
        @Valid
        List<AgregarLineaRequest> lineas
) {}
