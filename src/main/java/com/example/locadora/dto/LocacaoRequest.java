package com.example.locadora.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LocacaoRequest(
        @NotNull Long clienteId,
        @NotNull Long jogoId,
        @Min(1) int dias
) {
}
