package com.GesCom.dto.request;

import com.GesCom.enums.TipoPersona;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
        @NotNull(message = "El tipo de persona es obligatorio")
        TipoPersona tipoPersona,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El RIF o cédula es obligatorio")
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
