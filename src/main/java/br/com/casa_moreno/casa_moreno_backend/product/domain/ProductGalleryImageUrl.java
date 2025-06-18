package br.com.casa_moreno.casa_moreno_backend.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_gallery_image_urls")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ProductGalleryImageUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}