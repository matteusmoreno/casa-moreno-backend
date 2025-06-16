package br.com.casa_moreno.casa_moreno_backend.dto;

import br.com.casa_moreno.casa_moreno_backend.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetailsResponse(
        UUID id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String category,
        String subCategory,
        String imageUrl,
        String condition,
        String link) {

    public ProductDetailsResponse(Product product) {
        this(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getCategory(),
                product.getSubCategory(),
                product.getImageUrl(),
                product.getCondition(),
                product.getLink()
        );
    }
}
