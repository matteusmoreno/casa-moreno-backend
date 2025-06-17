package br.com.casa_moreno.casa_moreno_backend.client;

import java.math.BigDecimal;
import java.util.List;

public record MercadoLivreScraperResponse(
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
        String stockStatus) {
}
