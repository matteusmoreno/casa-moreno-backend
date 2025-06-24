package br.com.casa_moreno.casa_moreno_backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank
        String token,

        @NotBlank
        String newPassword
) {}