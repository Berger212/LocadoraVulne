package com.example.locadora.service;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.dto.JogoRequest;
import com.example.locadora.dto.JogoResponse;
import com.example.locadora.entity.Jogo;
import com.example.locadora.entity.RoleType;
import com.example.locadora.exception.AccessDeniedBusinessException;
import com.example.locadora.exception.BusinessException;
import com.example.locadora.exception.NotFoundException;
import com.example.locadora.repository.JogoRepository;
import com.example.locadora.util.InputSanitizer;
import com.example.locadora.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JogoService {

    private final JogoRepository jogoRepository;
    private final InputSanitizer inputSanitizer;
    private final AppSecurityProperties securityProperties;

    public JogoService(JogoRepository jogoRepository, InputSanitizer inputSanitizer, AppSecurityProperties securityProperties) {
        this.jogoRepository = jogoRepository;
        this.inputSanitizer = inputSanitizer;
        this.securityProperties = securityProperties;
    }

    public List<JogoResponse> listar() {
        return jogoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JogoResponse criar(JogoRequest request) {
        validarJogo(request);
        Jogo jogo = new Jogo();
        preencher(jogo, request);
        return toResponse(jogoRepository.save(jogo));
    }

    public JogoResponse atualizar(Long id, JogoRequest request) {

        Jogo jogo = buscarOuErro(id);
        validarJogo(request);
        preencher(jogo, request);
        return toResponse(jogoRepository.save(jogo));
    }

    public void remover(Long id) {

        if (!SecurityUtil.hasRole(RoleType.ADMIN)) {
            throw new AccessDeniedBusinessException("Somente administradores podem remover jogos");
        }
        Jogo jogo = buscarOuErro(id);
        jogoRepository.delete(jogo);
    }

    public JogoResponse buscar(Long id) {
        return toResponse(buscarOuErro(id));
    }

    public Jogo buscarOuErro(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Jogo não encontrado"));
    }

    private void validarJogo(JogoRequest request) {

        if (request.precoDiaria() == null || request.precoDiaria().doubleValue() <= 0) {
            throw new BusinessException("Preço inválido");
        }
    }

    private void preencher(Jogo jogo, JogoRequest request) {
        jogo.setTitulo(inputSanitizer.sanitize(request.titulo()));
        jogo.setGenero(inputSanitizer.sanitize(request.genero()));
        jogo.setDescricao(inputSanitizer.sanitize(request.descricao()));
        jogo.setPrecoDiaria(request.precoDiaria());
    }

    private JogoResponse toResponse(Jogo jogo) {
        return new JogoResponse(jogo.getId(), jogo.getTitulo(), jogo.getGenero(), jogo.getPrecoDiaria(), jogo.isDisponivel(), jogo.getDescricao());
    }
}
