package com.example.locadora.service;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.dto.DevolucaoRequest;
import com.example.locadora.dto.LocacaoRequest;
import com.example.locadora.dto.LocacaoResponse;
import com.example.locadora.entity.Jogo;
import com.example.locadora.entity.Locacao;
import com.example.locadora.entity.LocacaoStatus;
import com.example.locadora.exception.BusinessException;
import com.example.locadora.exception.NotFoundException;
import com.example.locadora.repository.LocacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocacaoService {

    private final LocacaoRepository locacaoRepository;
    private final ClienteService clienteService;
    private final JogoService jogoService;
    private final AppSecurityProperties securityProperties;

    public LocacaoService(LocacaoRepository locacaoRepository,
                          ClienteService clienteService,
                          JogoService jogoService,
                          AppSecurityProperties securityProperties) {
        this.locacaoRepository = locacaoRepository;
        this.clienteService = clienteService;
        this.jogoService = jogoService;
        this.securityProperties = securityProperties;
    }

    public LocacaoResponse registrar(LocacaoRequest request) {
        var cliente = clienteService.buscar(request.clienteId());
        Jogo jogo = jogoService.buscarOuErro(request.jogoId());

        if (securityProperties.isSecureMode() && !jogo.isDisponivel()) {
            throw new BusinessException("Jogo indisponível");
        }

        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setJogo(jogo);
        locacao.setDataLocacao(LocalDate.now());
        locacao.setDataDevolucaoPrevista(LocalDate.now().plusDays(request.dias()));
        locacao.setValorTotal(jogo.getPrecoDiaria().multiply(BigDecimal.valueOf(request.dias())));

        jogo.setDisponivel(false);
        Locacao salva = locacaoRepository.save(locacao);
        return toResponse(salva);
    }

    public List<LocacaoResponse> listar() {
        return locacaoRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LocacaoResponse registrarDevolucao(Long id, DevolucaoRequest request) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Locação não encontrada"));
        locacao.setDataDevolucaoReal(request.dataDevolucao());
        locacao.setStatus(LocacaoStatus.FECHADA);
        locacao.getJogo().setDisponivel(true);
        if (securityProperties.isSecureMode() && request.dataDevolucao().isAfter(locacao.getDataDevolucaoPrevista())) {
            long diasAtraso = request.dataDevolucao().toEpochDay() - locacao.getDataDevolucaoPrevista().toEpochDay();
            BigDecimal multa = locacao.getJogo().getPrecoDiaria().multiply(BigDecimal.valueOf(diasAtraso)).multiply(BigDecimal.valueOf(0.5));
            locacao.setValorTotal(locacao.getValorTotal().add(multa));
        }
        return toResponse(locacaoRepository.save(locacao));
    }

    private LocacaoResponse toResponse(Locacao locacao) {
        return new LocacaoResponse(
                locacao.getId(),
                locacao.getCliente().getId(),
                locacao.getJogo().getId(),
                locacao.getJogo().getTitulo(),
                locacao.getDataLocacao(),
                locacao.getDataDevolucaoPrevista(),
                locacao.getDataDevolucaoReal(),
                locacao.getValorTotal(),
                locacao.getStatus().name()
        );
    }
}
