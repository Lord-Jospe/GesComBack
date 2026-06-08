package com.GesCom.dto.request;

import com.GesCom.enums.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
        @NotBlank(message = "El primer nombre es obligatorio")
        String primerNombre,

        String segundoNombre,

        @NotBlank(message = "El primer apellido es obligatorio")
        String primerApellido,

        String segundoApellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        NombreRol rol,  // "ADMIN", "CONTADOR", "OPERADOR"

        java.math.BigDecimal sueldo,
        String monedaSueldo
) {}
