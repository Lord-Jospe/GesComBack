package com.GesCom.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UsuarioResponse(
        Long usuarioId,
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String email,
        String rol,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean activo,
        java.math.BigDecimal sueldo,
        String monedaSueldo
) {
}
