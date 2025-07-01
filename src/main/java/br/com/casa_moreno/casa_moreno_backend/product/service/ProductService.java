package br.com.casa_moreno.casa_moreno_backend.product.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperRequest;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.domain.ProductGalleryImageUrl;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.product.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        MercadoLivreScraperResponse scraperResponse = mercadoLivreScraperClient.getProductInfo(request.mercadoLivreUrl());

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
                .isPromotional(false)
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
        return productRepository.findAllByProductCategoryIgnoreCase(pageable, category).map(ProductDetailsResponse::new);
    }

    public List<ProductDetailsResponse> listAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDetailsResponse::new)
                .collect(Collectors.toList());
    }

    public Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + id + "' does not exist."));
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
    public void updateProductFromSync(Map<String, Object> updates) {
        if (!updates.containsKey("productId")) {
            throw new IllegalArgumentException("O payload de atualização deve conter um 'productId'.");
        }

        UUID productId = UUID.fromString(updates.get("productId").toString());
        Product product = findProductById(productId);

        ObjectMapper mapper = new ObjectMapper();
        updates.forEach((key, value) -> {
            switch (key) {
                case "currentPrice":
                    product.setCurrentPrice(mapper.convertValue(value, BigDecimal.class));
                    break;
                case "originalPrice":
                    product.setOriginalPrice(mapper.convertValue(value, BigDecimal.class));
                    break;
                case "discountPercentage":
                    product.setDiscountPercentage(mapper.convertValue(value, String.class));
                    break;
                case "installments":
                    product.setInstallments(mapper.convertValue(value, Integer.class));
                    break;
                case "installmentValue":
                    product.setInstallmentValue(mapper.convertValue(value, BigDecimal.class));
                    break;
                case "stockStatus":
                    product.setStockStatus(mapper.convertValue(value, String.class));
                    break;
                case "productId":
                    break;
                default:
                    break;
            }
        });

        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + productId + "' does not exist."));

        productRepository.delete(product);
    }

    public List<String> getDistinctCategories() {
        return productRepository.findDistinctProductCategories();
    }

    @Transactional
    public void updatePromotionalStatus(UUID productId, Boolean isPromotional) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + productId + "' does not exist."));

        product.setIsPromotional(isPromotional);
        productRepository.save(product);
    }

    public List<ProductDetailsResponse> findAllPromotionalProducts() {
        return productRepository.findAllByIsPromotionalTrue().stream()
                .map(ProductDetailsResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setMainProductImage(UUID productId, String newMainImageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + productId + "' does not exist."));

        List<ProductGalleryImageUrl> gallery = product.getGalleryImageUrls();
        ProductGalleryImageUrl imageToMove = null;
        int originalIndex = -1;

        // Encontra a imagem na lista
        for (int i = 0; i < gallery.size(); i++) {
            if (gallery.get(i).getImageUrl().equals(newMainImageUrl)) {
                imageToMove = gallery.get(i);
                originalIndex = i;
                break;
            }
        }

        if (imageToMove != null && originalIndex != 0) { // Se a imagem foi encontrada e não é a primeira
            gallery.remove(originalIndex); // Remove da posição original
            gallery.addFirst(imageToMove); // Adiciona na primeira posição
            productRepository.save(product); // Salva as alterações para persistir a nova ordem
        } else if (imageToMove == null) {
            throw new ProductNotFoundException("Image with URL '" + newMainImageUrl + "' not found in product gallery.");
        }
    }

    @Transactional
    public void deleteProductImage(UUID productId, String imageUrlToDelete) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID '" + productId + "' does not exist."));

        List<ProductGalleryImageUrl> gallery = product.getGalleryImageUrls();
        boolean removed = gallery.removeIf(image -> image.getImageUrl().equals(imageUrlToDelete));

        if (!removed) {
            throw new ProductNotFoundException("Image with URL '" + imageUrlToDelete + "' not found in product gallery.");
        }
        productRepository.save(product); // Salva para remover a imagem do banco de dados
    }

    private boolean isProvided(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
