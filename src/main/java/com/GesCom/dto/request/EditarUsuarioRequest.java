package com.GesCom.dto.request;

import com.GesCom.enums.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarUsuarioRequest(
        @Size(max = 50)
        String primerNombre,

        @Size(max = 50)
        String segundoNombre,

        @Size(max = 50)
        String primerApellido,

        @Size(max = 50)
        String segundoApellido,

        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String email,

        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        NombreRol rol

) {
}
