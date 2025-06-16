package br.com.casa_moreno.casa_moreno_backend.client;

import jakarta.validation.constraints.NotBlank;

public record MercadoLivreScraperRequest(
        @NotBlank
        String url) {
}
