package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EditarEmpresaRequest(
        @Size(max = 100)
        String nombre,

        @Size(max = 20)
        String rif,

        @Size(max = 255)
        String direccion,

        @Size(max = 20)
        String telefono,

        @Size(max = 100)
        String actividad,

        @Size(max = 500)
        String logoUrl,

        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String correo,

        // Configuración fiscal
        Boolean ivaActivo,
        BigDecimal ivaPorcentaje,
        Boolean igtfActivo,

        // Numeración de facturas
        @Size(max = 10)
        String facturaPrefijo,
        Integer facturaSiguienteNumero,

        // Deducciones de nómina
        BigDecimal ssoPorcentaje,
        BigDecimal incesPorcentaje,
        BigDecimal faovPorcentaje,

        // Stock
        Integer stockMinimoDefault
) {
}
