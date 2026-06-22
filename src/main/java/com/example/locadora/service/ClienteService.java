package com.example.locadora.service;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.dto.ClienteRequest;
import com.example.locadora.dto.ClienteResponse;
import com.example.locadora.entity.Cliente;
import com.example.locadora.exception.BusinessException;
import com.example.locadora.exception.NotFoundException;
import com.example.locadora.repository.ClienteRepository;
import com.example.locadora.util.DataProtectionService;
import com.example.locadora.util.InputSanitizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final DataProtectionService dataProtectionService;
    private final InputSanitizer inputSanitizer;
    private final AppSecurityProperties securityProperties;

    public ClienteService(ClienteRepository clienteRepository,
                          DataProtectionService dataProtectionService,
                          InputSanitizer inputSanitizer,
                          AppSecurityProperties securityProperties) {
        this.clienteRepository = clienteRepository;
        this.dataProtectionService = dataProtectionService;
        this.inputSanitizer = inputSanitizer;
        this.securityProperties = securityProperties;
    }

    public ClienteResponse criar(ClienteRequest request) {
        if (securityProperties.isSecureMode() && clienteRepository.findAll().stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(request.email()))) {
            throw new BusinessException("Cliente já cadastrado");
        }
        Cliente cliente = new Cliente();
        preencher(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Cliente buscar(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
    }

    private void preencher(Cliente cliente, ClienteRequest request) {
        if (securityProperties.isSecureMode()) {
            cliente.setNome(inputSanitizer.sanitize(request.nome()));
            cliente.setEmail(inputSanitizer.sanitize(request.email()));
            cliente.setTelefone(inputSanitizer.sanitize(request.telefone()));
        } else {
            cliente.setNome(request.nome());
            cliente.setEmail(request.email());
            cliente.setTelefone(request.telefone());
        }
        cliente.setDocumento(dataProtectionService.protect(request.documento()));
    }

    private ClienteResponse toResponse(Cliente cliente) {
        String documento = dataProtectionService.reveal(cliente.getDocumento());
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), documento, cliente.getTelefone());
    }
}
