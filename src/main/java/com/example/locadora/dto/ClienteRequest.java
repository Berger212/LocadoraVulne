package com.example.locadora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank String nome,
        @Email String email,
        @NotBlank @Size(min = 11, max = 14) String documento,
        @NotBlank String telefone
) {
}
