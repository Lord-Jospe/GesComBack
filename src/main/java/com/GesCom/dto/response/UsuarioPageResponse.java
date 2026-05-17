package com.GesCom.dto.response;

import java.util.List;

public record UsuarioPageResponse(
        List<UsuarioResponse> contenido,
        int paginaActual,
        int totalPaginas,
        long totalElementos,
        int tamano,
        boolean esUltima
) {

}
