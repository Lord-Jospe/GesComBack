package com.GesCom.dto.response;

import lombok.Builder;

@Builder
public record PlanCuentaResponse(
        Long cuentaId,
        String codigo,
        String nombre,
        String tipoCuenta,
        Long cuentaPadreId,
        boolean activo,
        boolean esPredeterminada
) {}
