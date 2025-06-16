package br.com.casa_moreno.casa_moreno_backend.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperRequest;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
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
        MercadoLivreScraperResponse scraperResponse = mercadoLivreScraperClient.scrapeProducts(new MercadoLivreScraperRequest(request.url()));

        Product product = Product.builder()
                .name(isProvided(request.name()) ? request.name() : scraperResponse.name())
                .description(isProvided(request.description()) ? request.description() : scraperResponse.description())
                .brand(isProvided(request.brand()) ? request.brand() : scraperResponse.brand())
                .price(request.price() != null ? request.price() : scraperResponse.price())
                .category(isProvided(request.category()) ? request.category() : scraperResponse.category())
                .subCategory(isProvided(request.subCategory()) ? request.subCategory() : scraperResponse.subCategory())
                .imageUrl(request.imageUrl())
                .condition(isProvided(request.condition()) ? request.condition() : scraperResponse.condition())
                .build();

        if (productRepository.existsByName(product.getName())) {
            throw new ProductAlreadyExistsException("Product with name '" + product.getName() + "' already exists.");
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(UpdateProductRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductAlreadyExistsException("Product with ID '" + request.productId() + "' does not exist."));

        if (request.name() != null) product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.brand() != null) product.setBrand(request.brand());
        if (request.price() != null) product.setPrice(request.price());
        if (request.category() != null) product.setCategory(request.category());
        if (request.subCategory() != null) product.setSubCategory(request.subCategory());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.condition() != null) product.setCondition(request.condition());

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
