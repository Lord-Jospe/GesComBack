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

        // Configuración fiscal
        boolean ivaActivo,
        BigDecimal ivaPorcentaje,
        boolean igtfActivo,

        // Numeración de facturas
        String facturaPrefijo,
        Integer facturaSiguienteNumero,

        LocalDateTime createdAt
) {
}
