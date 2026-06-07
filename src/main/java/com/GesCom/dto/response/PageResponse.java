package com.GesCom.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record PageResponse<T>(
        List<T> contenido,
        int paginaActual,
        int totalPaginas,
        long totalElementos,
        int tamano,
        boolean esUltima
) {}
