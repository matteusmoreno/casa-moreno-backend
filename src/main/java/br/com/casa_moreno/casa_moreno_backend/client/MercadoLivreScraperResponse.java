package br.com.casa_moreno.casa_moreno_backend.client;

import java.math.BigDecimal;

public record MercadoLivreScraperResponse(
        String name,
        String description,
        String brand,
        BigDecimal price,
        String category,
        String subCategory,
        String condition) {
}
