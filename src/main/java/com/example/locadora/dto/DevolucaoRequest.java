package com.example.locadora.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DevolucaoRequest(
        @NotNull LocalDate dataDevolucao
) {
}
