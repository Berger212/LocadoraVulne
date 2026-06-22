package com.example.locadora.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LocacaoResponse(
        Long id,
        Long clienteId,
        Long jogoId,
        String jogoTitulo,
        LocalDate dataLocacao,
        LocalDate dataDevolucaoPrevista,
        LocalDate dataDevolucaoReal,
        BigDecimal valorTotal,
        String status
) {
}
