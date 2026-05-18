package com.GesCom.dto.response;

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
        LocalDateTime createdAt
) {
}
