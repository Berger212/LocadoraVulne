package com.example.locadora.dto;

import java.math.BigDecimal;

public record JogoResponse(Long id, String titulo, String genero, BigDecimal precoDiaria, boolean disponivel, String descricao) {
}
