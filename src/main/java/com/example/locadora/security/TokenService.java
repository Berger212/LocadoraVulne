package com.example.locadora.security;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.entity.RoleType;
import com.example.locadora.entity.Usuario;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, UsuarioSession> sessions = new ConcurrentHashMap<>();
    private final AppSecurityProperties securityProperties;

    public TokenService(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String emitToken(Usuario usuario) {
        if (securityProperties.isSecureMode()) {
            String token = UUID.randomUUID().toString();
            Instant expires = Instant.now().plus(2, ChronoUnit.HOURS);
            sessions.put(token, new UsuarioSession(usuario.getId(), usuario.getUsername(), usuario.getRole(), expires));
            return token;
        }
        // Token inseguro previsível, sem expiração
        return usuario.getUsername() + "::" + usuario.getRole();
    }

    public Optional<UsuarioSession> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (securityProperties.isSecureMode()) {
            UsuarioSession session = sessions.get(token);
            if (session == null) {
                return Optional.empty();
            }
            if (session.expiresAt() != null && session.expiresAt().isBefore(Instant.now())) {
                sessions.remove(token);
                return Optional.empty();
            }
            return Optional.of(session);
        }
        // Token inseguro: apenas divide o texto e confia
        String[] parts = token.split("::");
        if (parts.length < 2) {
            return Optional.empty();
        }
        RoleType role = RoleType.valueOf(parts[1]);
        return Optional.of(new UsuarioSession(null, parts[0], role, null));
    }
}
