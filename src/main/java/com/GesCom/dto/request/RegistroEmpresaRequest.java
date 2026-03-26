package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroEmpresaRequest(
        // -- Datos de la empresa --
        @NotBlank(message = "El nombre comercial es obligatorio")
        String nombreEmpresa,

        @NotBlank(message = "El RIF es obligatorio")
        String rif,

        @NotBlank(message = "El correo de la empresa es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correoEmpresa,

        // Plan inicial: "SEMILLA", "EMPRENDEDOR", "NEGOCIO"
        @NotBlank(message = "El plan es obligatorio")
        String planNombre,

        // -- Datos del usuario Administrador --
        @NotBlank(message = "El primer nombre es obligatorio")
        String primerNombre,

        String segundoNombre,

        @NotBlank(message = "El primer apellido es obligatorio")
        String primerApellido,

        String segundoApellido,

        @NotBlank(message = "El correo del administrador es obligatorio")
        @Email(message = "Formato de correo inválido")
        String emailAdmin,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {
}
