package com.GesCom.dto.response;

import lombok.Builder;

@Builder
public record UsuarioResponse(
        Long usuarioId,
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String email,
        String rol,
        boolean activo
) {
}
