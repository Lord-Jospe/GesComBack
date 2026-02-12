package com.GesCom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest(
    @NotBlank(message = "Ingrese el correo correctamente")
    @Email(message = "Debe ser un correo valido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos seis caracteres")
    String password
    ){}
