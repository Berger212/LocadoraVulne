package com.example.locadora.security;

import com.example.locadora.entity.RoleType;

import java.time.Instant;

public record UsuarioSession(Long userId, String username, RoleType role, Instant expiresAt) {
}
