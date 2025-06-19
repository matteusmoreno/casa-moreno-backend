package br.com.casa_moreno.casa_moreno_backend.product.controller;

import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.product.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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

    @GetMapping("/find-by-category")
    public ResponseEntity<Page<ProductDetailsResponse>> findProductsByCategory(@PageableDefault(size = 10, sort = "productTitle") Pageable pageable, @RequestParam("category") String category) {
        Page<ProductDetailsResponse> products = productService.findProductsByCategory(pageable, category);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/list-all")
    public ResponseEntity<List<ProductDetailsResponse>> listAllProducts() {
        List<ProductDetailsResponse> products = productService.listAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsResponse> findProductById(@PathVariable UUID id) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(new ProductDetailsResponse(product));
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDetailsResponse> update(@RequestBody @Valid UpdateProductRequest request) {
        Product product = productService.updateProduct(request);

        return ResponseEntity.ok(new ProductDetailsResponse(product));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/{id}/promotional")
    public ResponseEntity<Void> setPromotionalStatus(@PathVariable UUID id, @RequestParam Boolean status) {
        productService.updatePromotionalStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promotional")
    public ResponseEntity<List<ProductDetailsResponse>> getPromotionalProducts() {
        List<ProductDetailsResponse> promotionalProducts = productService.findAllPromotionalProducts();
        return ResponseEntity.ok(promotionalProducts);
    }
}
