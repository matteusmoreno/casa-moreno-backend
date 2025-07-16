package br.com.casa_moreno.casa_moreno_backend.login.dto;

import java.time.LocalDateTime;

public record LoginResponse(
        String token,
        LocalDateTime expiresAt) {
}
