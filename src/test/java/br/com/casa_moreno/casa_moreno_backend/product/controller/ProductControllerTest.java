package br.com.casa_moreno.casa_moreno_backend.product.controller;

import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.domain.ProductGalleryImageUrl;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.product.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ProductService productService;

    CreateProductRequest createProductRequest = new CreateProductRequest(
                "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product",
                "This is a full description of the test product", "Brand", "New", BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00),
                "16% OFF", 3, BigDecimal.valueOf(33.33), List.of("https://image1.com", "https://image2.com"), "in stock",
                "https://affiliate-link.com", "Electronics", "Smartphones"
        );

    @Test
    @DisplayName("Should create a product successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProductSuccessfully() throws Exception {
        List<ProductGalleryImageUrl> productsGallery = new ArrayList<>();
        productsGallery.add(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null));
        productsGallery.add(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image2.com", null));

        Product savedProduct = Product.builder()
                .productId(UUID.randomUUID())
                .mercadoLivreId(createProductRequest.mercadoLivreId())
                .mercadoLivreUrl(createProductRequest.mercadoLivreUrl())
                .productTitle(createProductRequest.productTitle())
                .fullDescription(createProductRequest.fullDescription())
                .productBrand(createProductRequest.productBrand())
                .productCondition(createProductRequest.productCondition())
                .currentPrice(createProductRequest.currentPrice())
                .originalPrice(createProductRequest.originalPrice())
                .discountPercentage(createProductRequest.discountPercentage())
                .installments(createProductRequest.installments())
                .installmentValue(createProductRequest.installmentValue())
                .galleryImageUrls(productsGallery)
                .stockStatus(createProductRequest.stockStatus())
                .affiliateLink(createProductRequest.affiliateLink())
                .productCategory(createProductRequest.productCategory())
                .productSubcategory(createProductRequest.productSubcategory())
                .isPromotional(false)
                .build();

        when(productService.createProduct(createProductRequest)).thenReturn(savedProduct);

        mockMvc.perform(
                post("/products/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNotEmpty())
                .andExpect(jsonPath("$.productId").value(savedProduct.getProductId().toString()))
                .andExpect(jsonPath("$.productTitle").value("Test Product"))
                .andExpect(jsonPath("$.mercadoLivreId").value("ML12345"));

        verify(productService, times(1)).createProduct(createProductRequest);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when creating a product without authentication")
    void shouldReturnUnauthorizedWhenCreatingProductWithoutAuth() throws Exception {

        mockMvc.perform(
                        post("/products/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user with USER role tries to create a product")
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserRoleTriesToCreateProduct() throws Exception {
        mockMvc.perform(
                        post("/products/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isForbidden());

        verify(productService, never()).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should return 409 when product already exists")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflictWhenProductAlreadyExists() throws Exception {
        doThrow(new ProductAlreadyExistsException("Product with MercadoLivre ID ML12345 already exists."))
                .when(productService)
                .createProduct(any(CreateProductRequest.class));

        mockMvc.perform(
                        post("/products/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isConflict());

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Should find products by category successfully")
    void shouldFindProductsByCategorySuccessfully() throws Exception {
        String category = "Electronics";
        Pageable pageable = PageRequest.of(0, 10);

        List<Product> products = new ArrayList<>();
        products.add(new Product(UUID.randomUUID(), "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product", "This is a full description of the test product", "Brand", "New", BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00), "16% OFF", 3, BigDecimal.valueOf(33.33), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Smartphones", false));
        products.add(new Product(UUID.randomUUID(), "ML67890", "https://mercadolivre.com.br/product/67890", "Another Product", "This is another product description", "Brand", "New", BigDecimal.valueOf(200.00), BigDecimal.valueOf(250.00), "20% OFF", 2, BigDecimal.valueOf(100.00), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image2.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Laptops", false));
        products.add(new Product(UUID.randomUUID(), "ML54321", "https://mercadolivre.com.br/product/54321", "Third Product", "This is the third product description", "Brand", "New", BigDecimal.valueOf(150.00), BigDecimal.valueOf(180.00), "15% OFF", 1, BigDecimal.valueOf(150.00), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image3.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Tablets", false));

        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        Page<ProductDetailsResponse> responsePage = productPage.map(ProductDetailsResponse::new);

        when(productService.findProductsByCategory(any(Pageable.class), eq(category))).thenReturn(responsePage);

        mockMvc.perform(
                get("/products/find-by-category")
                        .param("category", category)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].productTitle").value("Test Product"))
                .andExpect(jsonPath("$.content[1].productTitle").value("Another Product"))
                .andExpect(jsonPath("$.content[2].productTitle").value("Third Product"));

        verify(productService, times(1)).findProductsByCategory(any(Pageable.class), eq(category));
    }

    @Test
    @DisplayName("Should return an empty page when category has no products")
    void shouldReturnEmptyPageForCategoryWithNoProducts() throws Exception {
        String category = "NonExistentCategory";
        Pageable pageable = PageRequest.of(0, 10);

        when(productService.findProductsByCategory(any(Pageable.class), eq(category)))
                .thenReturn(Page.empty(pageable));

        mockMvc.perform(
                get("/products/find-by-category")
                        .param("category", category)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Should list all products successfully")
    void shouldListAllProductsSuccessfully() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(UUID.randomUUID(), "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product", "This is a full description of the test product", "Brand", "New", BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00), "16% OFF", 3, BigDecimal.valueOf(33.33), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Smartphones", false));
        products.add(new Product(UUID.randomUUID(), "ML67890", "https://mercadolivre.com.br/product/67890", "Another Product", "This is another product description", "Brand", "New", BigDecimal.valueOf(200.00), BigDecimal.valueOf(250.00), "20% OFF", 2, BigDecimal.valueOf(100.00), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image2.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Laptops", false));

        List<ProductDetailsResponse> responseList = products.stream().map(ProductDetailsResponse::new).toList();

        when(productService.listAllProducts()).thenReturn(responseList);

        mockMvc.perform(
                get("/products/list-all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productTitle").value("Test Product"))
                .andExpect(jsonPath("$[1].productTitle").value("Another Product"));

        verify(productService, times(1)).listAllProducts();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        when(productService.listAllProducts()).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/products/list-all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).listAllProducts();
    }

    @Test
    @DisplayName("Should find product by ID successfully")
    void shouldFindProductByIdSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product", "This is a full description of the test product", "Brand", "New", BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00), "16% OFF", 3, BigDecimal.valueOf(33.33), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Smartphones", false);

        when(productService.findProductById(productId)).thenReturn(product);

        mockMvc.perform(
                get("/products/{id}", productId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.productTitle").value("Test Product"));

        verify(productService, times(1)).findProductById(productId);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.findProductById(productId)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(
                get("/products/{id}", productId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).findProductById(productId);
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                productId,
                "ML12345",
                "https://mercadolivre.com.br/product/12345",
                "Updated Product",
                "This is an updated full description of the test product",
                "Updated Brand",
                "New",
                BigDecimal.valueOf(110.00),
                BigDecimal.valueOf(130.00),
                "15% OFF",
                4,
                BigDecimal.valueOf(27.50),
                List.of(),
                "in stock",
                "https://affiliate-link.com",
                "Electronics",
                "Smartphones"
        );

        Product updatedProduct = Product.builder()
                .productId(productId)
                .mercadoLivreId(updateRequest.mercadoLivreId())
                .mercadoLivreUrl(updateRequest.mercadoLivreUrl())
                .productTitle(updateRequest.productTitle())
                .fullDescription(updateRequest.fullDescription())
                .productBrand(updateRequest.productBrand())
                .productCondition(updateRequest.productCondition())
                .currentPrice(updateRequest.currentPrice())
                .originalPrice(updateRequest.originalPrice())
                .discountPercentage(updateRequest.discountPercentage())
                .installments(updateRequest.installments())
                .installmentValue(updateRequest.installmentValue())
                .galleryImageUrls(new ArrayList<>())
                .stockStatus(updateRequest.stockStatus())
                .affiliateLink(updateRequest.affiliateLink())
                .productCategory(updateRequest.productCategory())
                .productSubcategory(updateRequest.productSubcategory())
                .isPromotional(false)
                .build();

        when(productService.updateProduct(updateRequest)).thenReturn(updatedProduct);

        mockMvc.perform(
                        put("/products/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.productTitle").value("Updated Product"))
                .andExpect(jsonPath("$.mercadoLivreId").value("ML12345"))
                .andExpect(jsonPath("$.productBrand").value("Updated Brand"))
                .andExpect(jsonPath("$.currentPrice").value(110.00))
                .andExpect(jsonPath("$.originalPrice").value(130.00))
                .andExpect(jsonPath("$.discountPercentage").value("15% OFF"))
                .andExpect(jsonPath("$.installments").value(4))
                .andExpect(jsonPath("$.installmentValue").value(27.50))
                .andExpect(jsonPath("$.stockStatus").value("in stock"))
                .andExpect(jsonPath("$.affiliateLink").value("https://affiliate-link.com"))
                .andExpect(jsonPath("$.productCategory").value("Electronics"))
                .andExpect(jsonPath("$.productSubcategory").value("Smartphones"))
                .andExpect(jsonPath("$.isPromotional").value(false));

        verify(productService, times(1)).updateProduct(updateRequest);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existing product")
    void shouldThrowProductNotFoundExceptionWhenUpdatingNonExistingProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                productId,
                "ML12345",
                "https://mercadolivre.com.br/product/12345",
                "Updated Product",
                "This is an updated full description of the test product",
                "Updated Brand",
                "New",
                BigDecimal.valueOf(110.00),
                BigDecimal.valueOf(130.00),
                "15% OFF",
                4,
                BigDecimal.valueOf(27.50),
                List.of(),
                "in stock",
                "https://affiliate-link.com",
                "Electronics",
                "Smartphones"
        );

        when(productService.updateProduct(updateRequest)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(
                        put("/products/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(updateRequest);
    }

    @Test
    @DisplayName("Should delete product successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteProductSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(
                delete("/products/delete/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated user tries to delete a product")
    void shouldReturnUnauthorizedWhenDeletingProductWithoutAuth() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(
                delete("/products/delete/{id}", productId))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).deleteProduct(any(UUID.class));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user with USER role tries to delete a product")
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserRoleTriesToDeleteProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(delete("/products/delete/{id}", productId))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(any(UUID.class));
    }


    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existing product")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowProductNotFoundExceptionWhenDeletingNonExistingProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        doThrow(new ProductNotFoundException("Product not found")).when(productService).deleteProduct(productId);

        mockMvc.perform(
                delete("/products/delete/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    @DisplayName("Should get distinct categories successfully")
    void shouldGetDistinctCategoriesSuccessfully() throws Exception {
        List<String> categories = List.of("Electronics", "Smartphones", "Laptops");

        when(productService.getDistinctCategories()).thenReturn(categories);

        mockMvc.perform(
                get("/products/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Electronics"))
                .andExpect(jsonPath("$[1]").value("Smartphones"))
                .andExpect(jsonPath("$[2]").value("Laptops"));

        verify(productService, times(1)).getDistinctCategories();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategoriesExist() throws Exception {
        when(productService.getDistinctCategories()).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/products/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).getDistinctCategories();
    }

    @Test
    @DisplayName("Should update promotional status successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdatePromotionalStatusSuccessfully() throws Exception {
        Product savedProduct = Product.builder()
                .productId(UUID.randomUUID())
                .mercadoLivreId(createProductRequest.mercadoLivreId())
                .mercadoLivreUrl(createProductRequest.mercadoLivreUrl())
                .productTitle(createProductRequest.productTitle())
                .fullDescription(createProductRequest.fullDescription())
                .productBrand(createProductRequest.productBrand())
                .productCondition(createProductRequest.productCondition())
                .currentPrice(createProductRequest.currentPrice())
                .originalPrice(createProductRequest.originalPrice())
                .discountPercentage(createProductRequest.discountPercentage())
                .installments(createProductRequest.installments())
                .installmentValue(createProductRequest.installmentValue())
                .galleryImageUrls(List.of())
                .stockStatus(createProductRequest.stockStatus())
                .affiliateLink(createProductRequest.affiliateLink())
                .productCategory(createProductRequest.productCategory())
                .productSubcategory(createProductRequest.productSubcategory())
                .isPromotional(false)
                .build();

        doNothing().when(productService).updatePromotionalStatus(savedProduct.getProductId(), true);

        mockMvc.perform(
                patch("/products/{id}/promotional", savedProduct.getProductId())
                        .param("status", "true"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).updatePromotionalStatus(savedProduct.getProductId(), true);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated user tries to update promotional status")
    void shouldReturnUnauthorizedWhenUpdatingPromotionalStatusWithoutAuth() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(
                patch("/products/{id}/promotional", productId)
                        .param("status", "true"))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).updatePromotionalStatus(any(UUID.class), anyBoolean());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user with USER role tries to update promotional status")
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserRoleTriesToUpdatePromotionalStatus() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(
                patch("/products/{id}/promotional", productId)
                        .param("status", "true"))
                .andExpect(status().isForbidden());

        verify(productService, never()).updatePromotionalStatus(any(UUID.class), anyBoolean());
    }

    @Test
    @DisplayName("Should return ProductNotFoundException when trying to update promotional status of non-existing product")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnProductNotFoundExceptionWhenUpdatingPromotionalStatusOfNonExistingProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        doThrow(new ProductNotFoundException("Product not found")).when(productService).updatePromotionalStatus(productId, true);

        mockMvc.perform(
                patch("/products/{id}/promotional", productId)
                        .param("status", "true"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updatePromotionalStatus(productId, true);
    }

    @Test
    @DisplayName("Should get promotional products successfully")
    void shouldGetPromotionalProductsSuccessfully() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product(UUID.randomUUID(), "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product", "This is a full description of the test product", "Brand", "New", BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00), "16% OFF", 3, BigDecimal.valueOf(33.33), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Smartphones", true));
        products.add(new Product(UUID.randomUUID(), "ML67890", "https://mercadolivre.com.br/product/67890", "Another Product", "This is another product description", "Brand", "New", BigDecimal.valueOf(200.00), BigDecimal.valueOf(250.00), "20% OFF", 2, BigDecimal.valueOf(100.00), List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image2.com", null)), "in stock", "https://affiliate-link.com", "Electronics", "Laptops", true));

        List<ProductDetailsResponse> responseList = products.stream().map(ProductDetailsResponse::new).toList();

        when(productService.findAllPromotionalProducts()).thenReturn(responseList);

        mockMvc.perform(
                get("/products/promotional")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productTitle").value("Test Product"))
                .andExpect(jsonPath("$[1].productTitle").value("Another Product"));

        verify(productService, times(1)).findAllPromotionalProducts();
    }

    @Test
    @DisplayName("Should return empty list when no promotional products exist")
    void shouldReturnEmptyListWhenNoPromotionalProductsExist() throws Exception {
        when(productService.findAllPromotionalProducts()).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/products/promotional")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).findAllPromotionalProducts();
    }

    @Test
    @DisplayName("Should set main product image successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldSetMainProductImageSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();
        String newMainImageUrl = "https://new-main-image.com/image.jpg";

        doNothing().when(productService).setMainProductImage(productId, newMainImageUrl);

        mockMvc.perform(
                patch("/products/{id}/images/set-main", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + newMainImageUrl + "\""))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).setMainProductImage(productId, newMainImageUrl);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated user tries to set main image")
    void shouldReturnUnauthorizedWhenSettingMainImageWithoutAuth() throws Exception {
        UUID productId = UUID.randomUUID();
        String newMainImageUrl = "https://new-main-image.com/image.jpg";

        mockMvc.perform(
                patch("/products/{id}/images/set-main", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + newMainImageUrl + "\""))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).setMainProductImage(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user with USER role tries to set main image")
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserRoleTriesToSetMainImage() throws Exception {
        UUID productId = UUID.randomUUID();
        String newMainImageUrl = "https://new-main-image.com/image.jpg";

        mockMvc.perform(
                patch("/products/{id}/images/set-main", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + newMainImageUrl + "\""))
                .andExpect(status().isForbidden());

        verify(productService, never()).setMainProductImage(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should return ProductNotFoundException when trying to set main image of non-existing product")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnProductNotFoundExceptionWhenSettingMainImageOfNonExistingProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        String newMainImageUrl = "https://new-main-image.com/image.jpg";

        doThrow(new ProductNotFoundException("Product not found")).when(productService).setMainProductImage(productId, newMainImageUrl);

        mockMvc.perform(
                patch("/products/{id}/images/set-main", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + newMainImageUrl + "\""))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).setMainProductImage(productId, newMainImageUrl);
    }

    @Test
    @DisplayName("Should delete product image successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteProductImageSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();
        String imageUrlToDelete = "https://image-to-delete.com/image.jpg";

        doNothing().when(productService).deleteProductImage(productId, imageUrlToDelete);

        mockMvc.perform(
                delete("/products/{id}/images/delete", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + imageUrlToDelete + "\""))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProductImage(productId, imageUrlToDelete);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated user tries to delete image")
    void shouldReturnUnauthorizedWhenDeletingImageWithoutAuth() throws Exception {
        UUID productId = UUID.randomUUID();
        String imageUrlToDelete = "https://image-to-delete.com/image.jpg";

        mockMvc.perform(
                delete("/products/{id}/images/delete", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + imageUrlToDelete + "\""))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).deleteProductImage(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user with USER role tries to delete image")
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserRoleTriesToDeleteImage() throws Exception {
        UUID productId = UUID.randomUUID();
        String imageUrlToDelete = "https://image-to-delete.com/image.jpg";

        mockMvc.perform(
                delete("/products/{id}/images/delete", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + imageUrlToDelete + "\""))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProductImage(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should return ProductNotFoundException when trying to delete image of non-existing product")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnProductNotFoundExceptionWhenDeletingImageOfNonExistingProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        String imageUrlToDelete = "https://image-to-delete.com/image.jpg";

        doThrow(new ProductNotFoundException("Product not found")).when(productService).deleteProductImage(productId, imageUrlToDelete);

        mockMvc.perform(
                delete("/products/{id}/images/delete", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + imageUrlToDelete + "\""))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).deleteProductImage(productId, imageUrlToDelete);
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable when Feign client cannot connect")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnServiceUnavailableForConnectException() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest(
                "ML123", "http://example.com/product", "New Product", "Description", "Brand",
                "New", BigDecimal.TEN, BigDecimal.TEN, "10%", 10, BigDecimal.ONE,
                List.of(), "in stock", "http://affiliate.com", "Electronics", "Gadgets"
        );

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new ConnectException("Failed to connect to the external service. Please try again later."));

        mockMvc.perform(
                post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Failed to connect to the external service. Please try again later."));
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable when Circuit Breaker is open")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnServiceUnavailableWhenCircuitBreakerIsOpen() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest(
                "ML123", "http://example.com/product", "New Product", "Description", "Brand",
                "New", BigDecimal.TEN, BigDecimal.TEN, "10%", 10, BigDecimal.ONE,
                List.of(), "in stock", "http://affiliate.com", "Electronics", "Gadgets"
        );

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(CallNotPermittedException.createCallNotPermittedException(
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("mercadoLivreScraper"))
                );

        mockMvc.perform(
                post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("The scraper service is external unavailable. Please try again later."));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }
}