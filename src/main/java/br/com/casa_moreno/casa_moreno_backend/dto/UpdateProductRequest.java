package br.com.casa_moreno.casa_moreno_backend.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String category,
        String subCategory,
        String imageUrl,
        String condition,
        String link) {
}
