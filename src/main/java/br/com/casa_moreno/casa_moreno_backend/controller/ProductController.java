package br.com.casa_moreno.casa_moreno_backend.controller;

import br.com.casa_moreno.casa_moreno_backend.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductDetailsResponse> create(@RequestBody @Valid CreateProductRequest request, UriComponentsBuilder uriBuilder) {
        Product product = productService.createProduct(request);
        URI uri = uriBuilder.path("/products/{id}").buildAndExpand(product.getProductId()).toUri();

        return ResponseEntity.created(uri).body(new ProductDetailsResponse(product));
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDetailsResponse> update(@RequestBody @Valid UpdateProductRequest request) {
        Product product = productService.updateProduct(request);

        return ResponseEntity.ok(new ProductDetailsResponse(product));
    }
}
