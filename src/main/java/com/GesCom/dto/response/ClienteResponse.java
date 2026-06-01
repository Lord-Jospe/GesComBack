package com.GesCom.dto.response;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long clienteId,
        String tipoPersona,
        String nombre,
        String rifCedula,
        String correo,
        String telefono,
        String direccion,
        boolean isActive,
        LocalDateTime createdAt
) {
}
