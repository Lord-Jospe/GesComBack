package com.GesCom.dto.request;

import com.GesCom.enums.TipoPersona;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record EditarClienteRequest(
        TipoPersona tipoPersona,

        @Size(max = 150)
        String nombre,

        @Size(max = 20)
        String rifCedula,

        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String correo,

        @Size(max = 20)
        String telefono,

        @Size(max = 255)
        String direccion
) {
}
