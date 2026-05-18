package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

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
        String correo
) {
}
