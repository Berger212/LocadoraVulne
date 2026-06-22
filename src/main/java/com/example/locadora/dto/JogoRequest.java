package com.example.locadora.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record JogoRequest(
        @NotBlank String titulo,
        String genero,
        @NotNull @DecimalMin("0.0") BigDecimal precoDiaria,
        String descricao
) {
}
