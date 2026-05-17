package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El primer nombre es obligatorio")
        @Size(max = 50)
        String primerNombre,

        @Size(max = 50)
        String segundoNombre,

        @NotBlank(message = "El primer apellido es obligatorio")
        @Size(max = 50)
        String primerApellido,

        @Size(max = 50)
        String segundoApellido,


        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Size(max = 20)
        String telefono,


        // Datos de la empresa
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 100)
        String nombreEmpresa,

        @NotBlank(message = "El RIF es obligatorio")
        @Size(max = 20)
        String rif

) {}