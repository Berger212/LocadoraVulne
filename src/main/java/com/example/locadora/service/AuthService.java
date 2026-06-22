package com.example.locadora.service;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.dto.LoginRequest;
import com.example.locadora.dto.LoginResponse;
import com.example.locadora.entity.Usuario;
import com.example.locadora.exception.BusinessException;
import com.example.locadora.repository.UsuarioRepository;
import com.example.locadora.security.TokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AppSecurityProperties securityProperties;

    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       AppSecurityProperties securityProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.securityProperties = securityProperties;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas"));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessException("Credenciais inválidas");
        }
        String token = tokenService.emitToken(usuario);
        return new LoginResponse(token, usuario.getRole().name(), securityProperties.getMode().name());
    }
}
