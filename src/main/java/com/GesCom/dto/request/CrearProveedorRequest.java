package com.GesCom.dto.request;

import com.GesCom.enums.CategoriaProveedor;
import jakarta.validation.constraints.*;


public record CrearProveedorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El RIF es obligatorio")
        @Size(max = 20)
        String rif,

        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String telefono,

        @NotNull(message = "La categoría es obligatoria")
        CategoriaProveedor categoria
) {
}
