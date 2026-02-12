package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El primer nombre es obligatorio")
        String primerNombre,

        String segundoNombre,

        @NotBlank(message = "El primer apellido es obligatorio")
        String primerApellido,

        String segundoApellido,

        String sexo,

        @NotBlank(message = "El rol es obligatorio")
        String rol  // "ADMIN", "DOCENTE", "ESTUDIANTE"
) {}