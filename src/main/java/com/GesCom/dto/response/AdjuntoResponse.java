package com.GesCom.dto.response;

import java.time.LocalDateTime;

public record AdjuntoResponse(
        Long adjuntoId,
        String nombreOriginal,
        String tipoArchivo,
        Long tamanio,
        Long transaccionId,
        String numeroFactura,
        LocalDateTime createdAt
) {}
