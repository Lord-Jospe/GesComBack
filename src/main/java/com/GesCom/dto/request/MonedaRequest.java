package com.GesCom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MonedaRequest(
        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "USD|VES", message = "La moneda debe ser USD o VES")
        String moneda
) {
}
