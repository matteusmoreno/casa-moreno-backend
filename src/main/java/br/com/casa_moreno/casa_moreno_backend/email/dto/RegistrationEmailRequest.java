package br.com.casa_moreno.casa_moreno_backend.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationEmailRequest(
        @NotBlank(message = "Recipient email is required.")
        @Email(message = "Invalid email format.")
        String to,

        @NotBlank(message = "Recipient name is required.")
        String name
) {}