package br.com.casa_moreno.casa_moreno_backend.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateUserRequest(
        @NotNull(message = "User ID is required")
        UUID userId,
        String name,
        String username,
        String password,
        String email,
        String phone) {
}
