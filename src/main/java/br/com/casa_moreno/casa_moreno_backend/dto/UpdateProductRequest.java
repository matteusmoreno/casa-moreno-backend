package br.com.casa_moreno.casa_moreno_backend.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        String mercadoLivreId,
        String mercadoLivreUrl,
        String productTitle,
        String fullDescription,
        String productBrand,
        String productCondition,
        BigDecimal currentPrice,
        BigDecimal originalPrice,
        String discountPercentage,
        Integer installments,
        BigDecimal installmentValue,
        List<String> galleryImageUrls,
        String stockStatus,
        String affiliateLink,
        String productCategory,
        String productSubcategory) {
}
