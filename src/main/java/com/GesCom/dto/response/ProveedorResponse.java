package com.GesCom.dto.response;

import java.time.LocalDateTime;

public record ProveedorResponse(
        Long proveedorId,
        String nombre,
        String rif,
        String email,
        String telefono,
        String categoria,
        boolean isActive,
        LocalDateTime createdAt
) {
}
