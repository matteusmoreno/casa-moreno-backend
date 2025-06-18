package br.com.casa_moreno.casa_moreno_backend.product.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperRequest;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.product.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MercadoLivreScraperClient mercadoLivreScraperClient;

    public ProductService(ProductRepository productRepository, MercadoLivreScraperClient mercadoLivreScraperClient) {
        this.productRepository = productRepository;
        this.mercadoLivreScraperClient = mercadoLivreScraperClient;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        MercadoLivreScraperResponse scraperResponse = mercadoLivreScraperClient.scrapeProducts(new MercadoLivreScraperRequest(request.mercadoLivreUrl()));

        Product product = Product.builder()
                .mercadoLivreId(isProvided(request.mercadoLivreId()) ? request.mercadoLivreId() : scraperResponse.mercadoLivreId())
                .mercadoLivreUrl(isProvided(request.mercadoLivreUrl()) ? request.mercadoLivreUrl() : scraperResponse.mercadoLivreUrl())
                .productTitle(isProvided(request.productTitle()) ? request.productTitle() : scraperResponse.productTitle())
                .fullDescription(isProvided(request.fullDescription()) ? request.fullDescription() : scraperResponse.fullDescription())
                .productBrand(isProvided(request.productBrand()) ? request.productBrand() : scraperResponse.productBrand())
                .productCondition(isProvided(request.productCondition()) ? request.productCondition() : scraperResponse.productCondition())
                .currentPrice(request.currentPrice() != null ? request.currentPrice() : scraperResponse.currentPrice())
                .originalPrice(request.originalPrice() != null ? request.originalPrice() : scraperResponse.originalPrice())
                .discountPercentage(isProvided(request.discountPercentage()) ? request.discountPercentage() : scraperResponse.discountPercentage())
                .installments(request.installments() != null ? request.installments() : scraperResponse.installments())
                .installmentValue(request.installmentValue() != null ? request.installmentValue() : scraperResponse.installmentValue())
                .stockStatus(isProvided(request.stockStatus()) ? request.stockStatus() : scraperResponse.stockStatus())
                .affiliateLink(request.affiliateLink())
                .productCategory(request.productCategory())
                .productSubcategory(request.productSubcategory())
                .build();

        if (request.galleryImageUrls() != null && !request.galleryImageUrls().isEmpty()) {
            product.setGalleryImageUrlsFromStrings(request.galleryImageUrls());
        } else if (scraperResponse.galleryImageUrls() != null && !scraperResponse.galleryImageUrls().isEmpty()) {
            product.setGalleryImageUrlsFromStrings(scraperResponse.galleryImageUrls());
        }

        if (productRepository.existsByProductTitle(product.getProductTitle()) || productRepository.existsByMercadoLivreId(product.getMercadoLivreId())) {
            throw new ProductAlreadyExistsException("Product is already exists");
        }

        return productRepository.save(product);
    }

    public Page<ProductDetailsResponse> findProductsByCategory(Pageable pageable, String category) {
        return productRepository.findAllByProductCategory(pageable, category).map(ProductDetailsResponse::new);
    }

    @Transactional
    public Product updateProduct(UpdateProductRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductAlreadyExistsException("Product with ID '" + request.productId() + "' does not exist."));

        if(request.mercadoLivreId() != null) product.setMercadoLivreId(request.mercadoLivreId());
        if(request.mercadoLivreUrl() != null) product.setMercadoLivreUrl(request.mercadoLivreUrl());
        if(request.productTitle() != null) product.setProductTitle(request.productTitle());
        if(request.fullDescription() != null) product.setFullDescription(request.fullDescription());
        if(request.productBrand() != null) product.setProductBrand(request.productBrand());
        if(request.productCondition() != null) product.setProductCondition(request.productCondition());
        if(request.currentPrice() != null) product.setCurrentPrice(request.currentPrice());
        if(request.originalPrice() != null) product.setOriginalPrice(request.originalPrice());
        if(request.discountPercentage() != null) product.setDiscountPercentage(request.discountPercentage());
        if(request.installments() != null) product.setInstallments(request.installments());
        if(request.installmentValue() != null) product.setInstallmentValue(request.installmentValue());
        if(request.galleryImageUrls() != null) product.setGalleryImageUrlsFromStrings(request.galleryImageUrls());
        if(request.stockStatus() != null) product.setStockStatus(request.stockStatus());
        if(request.affiliateLink() != null) product.setAffiliateLink(request.affiliateLink());
        if(request.productCategory() != null) product.setProductCategory(request.productCategory());
        if(request.productSubcategory() != null) product.setProductSubcategory(request.productSubcategory());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + productId + "' does not exist."));

        productRepository.delete(product);
    }

    private boolean isProvided(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
