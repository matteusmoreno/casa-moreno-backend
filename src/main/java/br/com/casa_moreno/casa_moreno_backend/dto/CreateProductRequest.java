package br.com.casa_moreno.casa_moreno_backend.dto;

import java.math.BigDecimal;

public record CreateProductRequest(
        String url,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String category,
        String subCategory,
        String imageUrl,
        String condition) {
}
