package br.com.casa_moreno.casa_moreno_backend.product.dto;

import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.domain.ProductGalleryImageUrl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ProductDetailsResponse(
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

    public ProductDetailsResponse(Product product) {
        this(
                product.getProductId(),
                product.getMercadoLivreId(),
                product.getMercadoLivreUrl(),
                product.getProductTitle(),
                product.getFullDescription(),
                product.getProductBrand(),
                product.getProductCondition(),
                product.getCurrentPrice(),
                product.getOriginalPrice(),
                product.getDiscountPercentage(),
                product.getInstallments(),
                product.getInstallmentValue(),

                product.getGalleryImageUrls() != null ?
                        product.getGalleryImageUrls().stream()
                                .map(ProductGalleryImageUrl::getImageUrl)
                                .collect(Collectors.toList()) :
                        Collections.emptyList(),

                product.getStockStatus(),
                product.getAffiliateLink(),
                product.getProductCategory(),
                product.getProductSubcategory()
        );
    }
}
