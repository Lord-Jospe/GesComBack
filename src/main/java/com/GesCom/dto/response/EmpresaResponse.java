package com.GesCom.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EmpresaResponse(
        Long empresaId,
        String nombre,
        String rif,
        String correo,
        String telefono,
        String direccion,
        String logoUrl,
        String actividad,
        String monedaBase,
        boolean isActive,

        boolean ivaActivo,
        BigDecimal ivaPorcentaje,
        boolean igtfActivo,

        String facturaPrefijo,
        Integer facturaSiguienteNumero,

        BigDecimal ssoPorcentaje,
        BigDecimal incesPorcentaje,
        BigDecimal faovPorcentaje,
        Integer stockMinimoDefault,

        LocalDateTime createdAt
) {
}
