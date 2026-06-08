package com.GesCom.dto.request;

import com.GesCom.enums.EstadoTransaccion;
import com.GesCom.enums.TipoTransaccion;

import java.time.LocalDate;

public record FiltroTransaccionRequest(
        TipoTransaccion tipo,
        EstadoTransaccion estado,
        Long clienteId,
        Long proveedorId,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {}
