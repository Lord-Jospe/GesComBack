package com.GesCom.dto.response;

public record RegistroResponse(
        String token,
        UsuarioResponse usuario
) {
}
