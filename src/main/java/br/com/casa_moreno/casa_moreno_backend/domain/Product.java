package br.com.casa_moreno.casa_moreno_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;
    private String mercadoLivreId;
    private String mercadoLivreUrl;
    private String productTitle;
    private String fullDescription;
    private String productBrand;
    private String productCondition;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private String discountPercentage;
    private Integer installments;
    private BigDecimal installmentValue;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductGalleryImageUrl> galleryImageUrls = new ArrayList<>();

    private String stockStatus;
    private String affiliateLink;
    private String productCategory;
    private String productSubcategory;

    public void addGalleryImageUrl(String imageUrl) {
        ProductGalleryImageUrl galleryUrl = ProductGalleryImageUrl.builder()
                .imageUrl(imageUrl)
                .product(this)
                .build();
        this.galleryImageUrls.add(galleryUrl);
    }

    public void setGalleryImageUrlsFromStrings(List<String> imageUrls) {
        this.galleryImageUrls.clear();
        if (imageUrls != null) {
            for (String url : imageUrls) {
                addGalleryImageUrl(url);
            }
        }
    }
}
