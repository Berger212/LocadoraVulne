package com.example.locadora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(min = 4, max = 40)
        String username,
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6, max = 200)
        String senha,
        @NotBlank(message = "nome é obrigatório")
        String nome,
        @NotBlank(message = "perfil é obrigatório")
        String role,
        @Email(message = "email inválido")
        String email
) {
}
