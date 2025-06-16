package br.com.casa_moreno.casa_moreno_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String category;
    private String subCategory;
    private String imageUrl;
    private String condition;
}
