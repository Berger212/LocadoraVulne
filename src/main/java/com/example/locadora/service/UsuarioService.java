package com.example.locadora.service;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.dto.UsuarioRequest;
import com.example.locadora.dto.UsuarioResponse;
import com.example.locadora.entity.RoleType;
import com.example.locadora.entity.Usuario;
import com.example.locadora.exception.BusinessException;
import com.example.locadora.repository.UsuarioRepository;
import com.example.locadora.util.InputSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;
    private final AppSecurityProperties securityProperties;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          InputSanitizer inputSanitizer,
                          AppSecurityProperties securityProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.inputSanitizer = inputSanitizer;
        this.securityProperties = securityProperties;
    }

    public UsuarioResponse criarUsuario(UsuarioRequest request) {
        if (securityProperties.isSecureMode()) {
            usuarioRepository.findByUsername(request.username()).ifPresent(u -> {
                throw new BusinessException("Usuário já existente");
            });
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setNome(securityProperties.isSecureMode() ? inputSanitizer.sanitize(request.nome()) : request.nome());
        usuario.setEmail(securityProperties.isSecureMode() ? inputSanitizer.sanitize(request.email()) : request.email());
        usuario.setRole(RoleType.valueOf(request.role().toUpperCase()));
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        Usuario salvo = usuarioRepository.save(usuario);
        return new UsuarioResponse(salvo.getId(), salvo.getUsername(), salvo.getNome(), salvo.getRole().name(), salvo.getEmail());
    }
}
