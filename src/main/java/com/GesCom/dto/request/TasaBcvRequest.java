package com.GesCom.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TasaBcvRequest(
        @NotNull(message = "La tasa es obligatoria")
        @DecimalMin(value = "0.0001", message = "La tasa debe ser mayor a 0")
        BigDecimal tasa,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha
) {}
