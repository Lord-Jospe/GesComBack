package com.GesCom.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        Long usuarioId,
        Long empresaId,
        String nombreCompleto,
        String rol,
        String nombreEmpresa
){
}

