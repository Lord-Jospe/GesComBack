package com.GesCom.dto.request;

import com.GesCom.enums.CategoriaProveedor;
import jakarta.validation.constraints.*;

public record EditarProveedorRequest(
        @Size(max = 150)
        String nombre,

        @Size(max = 20)
        String rif,

        @Email(message = "Formato de correo inválido")
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String telefono,

        CategoriaProveedor categoria
) {
}
