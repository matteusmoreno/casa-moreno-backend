package br.com.casa_moreno.casa_moreno_backend.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperRequest;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

        productRepository.save(product);
        return product;
    }

    private boolean isProvided(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
