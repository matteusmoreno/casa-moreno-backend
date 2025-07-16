package br.com.casa_moreno.casa_moreno_backend.product.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductNotFoundException;
import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.domain.ProductGalleryImageUrl;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
import br.com.casa_moreno.casa_moreno_backend.product.dto.UpdateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProductService Tests")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MercadoLivreScraperClient mercadoLivreScraperClient;
    @InjectMocks
    private ProductService productService;

    private MercadoLivreScraperResponse mercadoLivreScraperResponse;
    private CreateProductRequest createProductRequest;
    private Product iphoneProduct;
    private Product samsungProduct;

    @BeforeEach
    void setUp() {
        createProductRequest = new CreateProductRequest(
                "ML12345",
                "https://mercadolivre.com.br/product/12345",
                "Test Product",
                "This is a full description of the test product",
                "Brand",
                "New",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(120.00),
                "16% OFF",
                3,
                BigDecimal.valueOf(33.33),
                List.of("https://image1.com", "https://image2.com"),
                "in stock",
                "https://affiliate-link.com",
                "Electronics",
                "Smartphones"
        );

        mercadoLivreScraperResponse = new MercadoLivreScraperResponse(
                "ML12345",
                "https://mercadolivre.com.br/product/12345",
                "Test Product",
                "This is a full description of the test product.",
                "Brand",
                "New",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(120.00),
                "16% OFF",
                3,
                BigDecimal.valueOf(33.33),
                List.of("https://image1.com", "https://image2.com"),
                "in stock"
        );

        iphoneProduct = Product.builder()
                .productId(UUID.randomUUID())
                .mercadoLivreId("ML12345")
                .mercadoLivreUrl("https://mercadolivre.com.br/product/12345")
                .productTitle("iPhone 14 Pro")
                .fullDescription("Latest Apple iPhone with advanced features.")
                .productBrand("Apple")
                .productCondition("New")
                .currentPrice(BigDecimal.valueOf(999.99))
                .originalPrice(BigDecimal.valueOf(1099.99))
                .discountPercentage("9% OFF")
                .installments(12)
                .installmentValue(BigDecimal.valueOf(83.33))
                .galleryImageUrls(List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image1.com", null)))
                .stockStatus("in stock")
                .affiliateLink("https://affiliate-link.com/iphone14pro")
                .productCategory("Electronics")
                .productSubcategory("Smartphones")
                .isPromotional(false)
                .build();

        samsungProduct = Product.builder()
                .productId(UUID.randomUUID())
                .mercadoLivreId("ML67890")
                .mercadoLivreUrl("https://mercadolivre.com.br/product/67890")
                .productTitle("Samsung Galaxy S23")
                .fullDescription("Latest Samsung smartphone with cutting-edge technology.")
                .productBrand("Samsung")
                .productCondition("New")
                .currentPrice(BigDecimal.valueOf(799.99))
                .originalPrice(BigDecimal.valueOf(899.99))
                .discountPercentage("11% OFF")
                .installments(10)
                .installmentValue(BigDecimal.valueOf(79.99))
                .galleryImageUrls(List.of(new ProductGalleryImageUrl(UUID.randomUUID(), "https://image2.com", null)))
                .stockStatus("in stock")
                .affiliateLink("https://affiliate-link.com/samsungs23")
                .productCategory("Electronics")
                .productSubcategory("Smartphones")
                .isPromotional(false)
                .build();
    }

    @Test
    @DisplayName("Should create a product successfully")
    void shouldCreateProductSuccessfully() {
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

        when(mercadoLivreScraperClient.getProductInfo(createProductRequest.mercadoLivreUrl()))
                .thenReturn(mercadoLivreScraperResponse);
        when(productRepository.existsByProductTitle(createProductRequest.productTitle())).thenReturn(false);
        when(productRepository.existsByMercadoLivreId(createProductRequest.mercadoLivreId())).thenReturn(false);
        when(productRepository.save(Mockito.any(Product.class))).thenReturn(savedProduct);

        productService.createProduct(createProductRequest);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(mercadoLivreScraperClient, times(1)).getProductInfo(createProductRequest.mercadoLivreUrl());
        verify(productRepository, times(1)).existsByProductTitle(createProductRequest.productTitle());
        verify(productRepository, times(1)).existsByMercadoLivreId(createProductRequest.mercadoLivreId());
        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertAll(
                () -> assertNull(capturedProduct.getProductId(), "ID product should be null because the id is generated in the final line of the method"),
                () -> assertEquals(createProductRequest.mercadoLivreId(), capturedProduct.getMercadoLivreId()),
                () -> assertEquals(createProductRequest.mercadoLivreUrl(), capturedProduct.getMercadoLivreUrl()),
                () -> assertEquals(createProductRequest.productTitle(), capturedProduct.getProductTitle()),
                () -> assertEquals(createProductRequest.fullDescription(), capturedProduct.getFullDescription()),
                () -> assertEquals(createProductRequest.productBrand(), capturedProduct.getProductBrand()),
                () -> assertEquals(createProductRequest.productCondition(), capturedProduct.getProductCondition()),
                () -> assertEquals( createProductRequest.currentPrice(), capturedProduct.getCurrentPrice()),
                () -> assertEquals(createProductRequest.originalPrice(), capturedProduct.getOriginalPrice()),
                () -> assertEquals(createProductRequest.discountPercentage(), capturedProduct.getDiscountPercentage()),
                () -> assertEquals(createProductRequest.installments(), capturedProduct.getInstallments()),
                () -> assertEquals(createProductRequest.installmentValue(), capturedProduct.getInstallmentValue()),
                () -> assertEquals(createProductRequest.stockStatus(), capturedProduct.getStockStatus()),
                () -> assertEquals(createProductRequest.affiliateLink(), capturedProduct.getAffiliateLink()),
                () -> assertEquals(createProductRequest.productCategory(), capturedProduct.getProductCategory()),
                () -> assertEquals(createProductRequest.productSubcategory(), capturedProduct.getProductSubcategory()),
                () -> assertFalse(capturedProduct.getIsPromotional(), "The product should not be promotional by default"),
                () -> assertNotNull(capturedProduct.getGalleryImageUrls()),
                () -> assertEquals(2, capturedProduct.getGalleryImageUrls().size()),
                () -> assertEquals("https://image1.com", capturedProduct.getGalleryImageUrls().get(0).getImageUrl()),
                () -> assertEquals("https://image2.com", capturedProduct.getGalleryImageUrls().get(1).getImageUrl())
        );
    }

    @Test
    @DisplayName("Should create product using scraper data as fallback when request data is missing")
    void shouldCreateProductUsingScraperDataAsFallback() {
        CreateProductRequest incompleteRequest = new CreateProductRequest(
                null,        // mercadoLivreId
                null,        // mercadoLivreUrl
                null,        // productTitle
                null,        // fullDescription
                null,        // productBrand
                null,        // productCondition
                null,        // currentPrice
                null,        // originalPrice
                null,        // discountPercentage
                null,        // installments
                null,        // installmentValue
                List.of(),   // galleryImageUrls (empty list)
                null,        // stockStatus
                "https://affiliate-link-from-request.com", // affiliateLink
                "Electronics",                             // productCategory
                "Smartphones"                              // productSubcategory
        );

        when(mercadoLivreScraperClient.getProductInfo(incompleteRequest.mercadoLivreUrl()))
                .thenReturn(mercadoLivreScraperResponse);

        when(productRepository.existsByProductTitle(mercadoLivreScraperResponse.productTitle())).thenReturn(false);
        when(productRepository.existsByMercadoLivreId(mercadoLivreScraperResponse.mercadoLivreId())).thenReturn(false);

        productService.createProduct(incompleteRequest);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertAll("Verifica se os dados do scraper foram usados como fallback",
                () -> assertEquals(incompleteRequest.affiliateLink(), capturedProduct.getAffiliateLink()),
                () -> assertEquals(incompleteRequest.productCategory(), capturedProduct.getProductCategory()),

                () -> assertEquals(mercadoLivreScraperResponse.mercadoLivreId(), capturedProduct.getMercadoLivreId()),
                () -> assertEquals(mercadoLivreScraperResponse.mercadoLivreUrl(), capturedProduct.getMercadoLivreUrl()),
                () -> assertEquals(mercadoLivreScraperResponse.productTitle(), capturedProduct.getProductTitle()),
                () -> assertEquals(mercadoLivreScraperResponse.fullDescription(), capturedProduct.getFullDescription()),
                () -> assertEquals(mercadoLivreScraperResponse.productBrand(), capturedProduct.getProductBrand()),
                () -> assertEquals(mercadoLivreScraperResponse.currentPrice(), capturedProduct.getCurrentPrice()),
                () -> assertEquals(mercadoLivreScraperResponse.originalPrice(), capturedProduct.getOriginalPrice()),

                () -> {
                    assertNotNull(capturedProduct.getGalleryImageUrls(), "Gallery image should not be null");
                    assertEquals(mercadoLivreScraperResponse.galleryImageUrls().size(), capturedProduct.getGalleryImageUrls().size());
                    assertEquals(mercadoLivreScraperResponse.galleryImageUrls().get(0), capturedProduct.getGalleryImageUrls().get(0).getImageUrl());
                }
        );
    }

    @Test
    @DisplayName("Should create product with null gallery when both request and scraper galleries are null")
    void shouldCreateProductWithNullGalleryWhenGalleriesAreNull() {
        CreateProductRequest requestWithNullGallery = new CreateProductRequest(
                "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product",
                "Description", "Brand", "New", BigDecimal.TEN, BigDecimal.TEN,
                "10%", 1, BigDecimal.TEN, null,
                "in stock", "https://affiliate-link.com", "Electronics", "Gadgets"
        );

        MercadoLivreScraperResponse scraperResponseWithNullGallery = new MercadoLivreScraperResponse(
                "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product",
                "Description", "Brand", "New", BigDecimal.TEN, BigDecimal.TEN,
                "10%", 1, BigDecimal.TEN, null,
                "in stock"
        );

        when(mercadoLivreScraperClient.getProductInfo(requestWithNullGallery.mercadoLivreUrl()))
                .thenReturn(scraperResponseWithNullGallery);
        when(productRepository.existsByProductTitle(anyString())).thenReturn(false);
        when(productRepository.existsByMercadoLivreId(anyString())).thenReturn(false);

        productService.createProduct(requestWithNullGallery);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertNotNull(capturedProduct.getGalleryImageUrls());
        assertTrue(capturedProduct.getGalleryImageUrls().isEmpty());
    }

    @Test
    @DisplayName("Should create product with empty gallery when request gallery is null and scraper gallery is empty")
    void shouldCreateProductWithEmptyGalleryWhenRequestIsNullAndScraperIsEmpty() {
        CreateProductRequest requestWithNullGallery = new CreateProductRequest(
                "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product",
                "Description", "Brand", "New", BigDecimal.TEN, BigDecimal.TEN,
                "10%", 1, BigDecimal.TEN, null,
                "in stock", "https://affiliate-link.com", "Electronics", "Gadgets"
        );

        MercadoLivreScraperResponse scraperResponseWithEmptyGallery = new MercadoLivreScraperResponse(
                "ML12345", "https://mercadolivre.com.br/product/12345", "Test Product",
                "Description", "Brand", "New", BigDecimal.TEN, BigDecimal.TEN,
                "10%", 1, BigDecimal.TEN, List.of(),
                "in stock"
        );

        when(mercadoLivreScraperClient.getProductInfo(requestWithNullGallery.mercadoLivreUrl()))
                .thenReturn(scraperResponseWithEmptyGallery);
        when(productRepository.existsByProductTitle(anyString())).thenReturn(false);
        when(productRepository.existsByMercadoLivreId(anyString())).thenReturn(false);

        productService.createProduct(requestWithNullGallery);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertNotNull(capturedProduct.getGalleryImageUrls(), "The gallery must be a list object, not null");
        assertTrue(capturedProduct.getGalleryImageUrls().isEmpty(), "The gallery should be empty since no valid image sources were provided");
    }

    @Test
    @DisplayName("Should throw ProductAlreadyExistsException when product already exists by title")
    void shouldThrowProductAlreadyExistsExceptionWhenProductAlreadyExistsByTitle() {
        when(mercadoLivreScraperClient.getProductInfo(createProductRequest.mercadoLivreUrl()))
                .thenReturn(mercadoLivreScraperResponse);
        when(productRepository.existsByProductTitle(createProductRequest.productTitle())).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(createProductRequest));

        verify(mercadoLivreScraperClient, times(1)).getProductInfo(createProductRequest.mercadoLivreUrl());
        verify(productRepository, times(1)).existsByProductTitle(createProductRequest.productTitle());
        verify(productRepository, never()).existsByMercadoLivreId(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductAlreadyExistsException when product already exists by MercadoLivreId")
    void shouldThrowExceptionWhenProductExistsByMercadoLivreId() {
        when(mercadoLivreScraperClient.getProductInfo(createProductRequest.mercadoLivreUrl()))
                .thenReturn(mercadoLivreScraperResponse);
        when(productRepository.existsByProductTitle(createProductRequest.productTitle())).thenReturn(false);
        when(productRepository.existsByMercadoLivreId(createProductRequest.mercadoLivreId())).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> {
            productService.createProduct(createProductRequest);
        });

        verify(mercadoLivreScraperClient, times(1)).getProductInfo(createProductRequest.mercadoLivreUrl());
        verify(productRepository, times(1)).existsByProductTitle(createProductRequest.productTitle());
        verify(productRepository, times(1)).existsByMercadoLivreId(createProductRequest.mercadoLivreId());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return products by category paginated")
    void shouldReturnProductsByCategoryPaginated() {
        String category = "Smartphones";
        Pageable pageable = PageRequest.of(0, 10); // Página 0, com 10 itens
        List<Product> productList = List.of(iphoneProduct, samsungProduct);

        Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findAllByProductCategoryIgnoreCase(pageable, category))
                .thenReturn(productPage);

        Page<ProductDetailsResponse> resultPage = productService.findProductsByCategory(pageable, category);

        verify(productRepository, times(1)).findAllByProductCategoryIgnoreCase(pageable, category);

        List<ProductDetailsResponse> content = resultPage.getContent();
        ProductDetailsResponse firstProduct = content.get(0);
        ProductDetailsResponse secondProduct = content.get(1);

        assertAll("Verify pagination metadata and content",
                // General Asserts
                () -> assertNotNull(resultPage, "The result page should not be null."),
                () -> assertEquals(2, resultPage.getTotalElements(), "Total elements should be 2."),
                () -> assertEquals(1, resultPage.getTotalPages(), "Total pages should be 1."),
                () -> assertEquals(2, content.size(), "The content list should have 2 products."),

                // Product 0 Asserts (iPhone)
                () -> assertEquals(iphoneProduct.getProductId(), firstProduct.productId(), "Product 0 ID should match."),
                () -> assertEquals(iphoneProduct.getProductTitle(), firstProduct.productTitle(), "Product 0 title should match."),
                () -> assertEquals(iphoneProduct.getProductBrand(), firstProduct.productBrand(), "Product 0 brand should match."),
                () -> assertEquals(0, iphoneProduct.getCurrentPrice().compareTo(firstProduct.currentPrice()), "Product 0 current price should match."),

                // Product 1 Asserts (Samsung)
                () -> assertEquals(samsungProduct.getProductId(), secondProduct.productId(), "Product 1 ID should match."),
                () -> assertEquals(samsungProduct.getProductTitle(), secondProduct.productTitle(), "Product 1 title should match."),
                () -> assertEquals(samsungProduct.getProductBrand(), secondProduct.productBrand(), "Product 1 brand should match."),
                () -> assertEquals(0, samsungProduct.getCurrentPrice().compareTo(secondProduct.currentPrice()), "Product 1 current price should match.")
        );
    }

    @Test
    @DisplayName("Should return products when searching with a different case category")
    void shouldReturnProductsWhenSearchingWithDifferentCaseCategory() {
        String categoryWithDifferentCase = "smartphones"; // Categoria em minúsculas
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = List.of(iphoneProduct, samsungProduct); // Seus produtos com categoria "Smartphones"
        Page<Product> productPageFromRepo = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findAllByProductCategoryIgnoreCase(pageable, categoryWithDifferentCase))
                .thenReturn(productPageFromRepo);

        Page<ProductDetailsResponse> resultPage = productService.findProductsByCategory(pageable, categoryWithDifferentCase);

        assertEquals(2, resultPage.getTotalElements(), "Should find 2 products even with different case.");
        assertEquals("iPhone 14 Pro", resultPage.getContent().get(0).productTitle());
    }

    @Test
    @DisplayName("Should return an empty page when no products match the category")
    void shouldReturnEmptyPageWhenNoProductsMatchCategory() {
        String category = "NonExistentCategory";
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAllByProductCategoryIgnoreCase(pageable, category))
                .thenReturn(Page.empty(pageable));

        Page<ProductDetailsResponse> resultPage = productService.findProductsByCategory(pageable, category);

        assertNotNull(resultPage, "The result page should not be null, even if empty.");
        assertTrue(resultPage.getContent().isEmpty(), "The content of the page should be empty.");
        assertEquals(0, resultPage.getTotalElements(), "Total elements should be 0.");
    }

    @Test
    @DisplayName("Should return all products successfully")
    void shouldReturnAllProductsSuccessfully() {
        List<Product> productList = List.of(iphoneProduct, samsungProduct);
        when(productRepository.findAll()).thenReturn(productList);

        List<ProductDetailsResponse> result = productService.listAllProducts();

        verify(productRepository, times(1)).findAll();

        assertAll(
                () -> assertNotNull(result, "The result list should not be null"),
                () -> assertEquals(2, result.size(), "The result list should contain 2 products"),
                () -> assertEquals(iphoneProduct.getProductId(), result.get(0).productId(), "First product ID should match iPhone product ID"),
                () -> assertEquals(samsungProduct.getProductId(), result.get(1).productId(), "Second product ID should match Samsung product ID")
        );
    }

    @Test
    @DisplayName("Should return an empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductDetailsResponse> result = productService.listAllProducts();

        verify(productRepository, times(1)).findAll();

        assertNotNull(result, "The result list should not be null");
        assertTrue(result.isEmpty(), "The result list should be empty when no products exist");
    }

    @Test
    @DisplayName("Should return product details by ID successfully")
    void shouldReturnProductDetailsByIdSuccessfully() {
        UUID productId = iphoneProduct.getProductId();

        when(productRepository.findById(productId))
                .thenReturn(java.util.Optional.of(iphoneProduct));

        Product result = productService.findProductById(productId);

        verify(productRepository, times(1)).findById(productId);

        assertAll(
                () -> assertNotNull(result, "The result product should not be null"),
                () -> assertEquals(iphoneProduct.getProductId(), result.getProductId(), "Product ID should match"),
                () -> assertEquals(iphoneProduct.getProductTitle(), result.getProductTitle(), "Product title should match"),
                () -> assertEquals(iphoneProduct.getCurrentPrice(), result.getCurrentPrice(), "Current price should match")
        );
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product ID does not exist")
    void shouldThrowProductNotFoundExceptionWhenProductIdDoesNotExist() {
        UUID nonExistentProductId = UUID.randomUUID();

        when(productRepository.findById(nonExistentProductId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findProductById(nonExistentProductId));

        verify(productRepository, times(1)).findById(nonExistentProductId);
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        UUID productId = iphoneProduct.getProductId();
        UpdateProductRequest updatedProductRequest = new UpdateProductRequest(
                productId,
                "Updated iPhone 14 Pro",
                "https://new-url.com",
                "ML-UPDATED", "Updated description", "Apple", "New",
                BigDecimal.valueOf(950.00), BigDecimal.valueOf(1050.00), "10% OFF",
                12, BigDecimal.valueOf(79.17),
                List.of("https://new-image.com"), "in stock", "https://new-affiliate.com",
                "Electronics", "Premium Smartphones"
        );

        iphoneProduct.setGalleryImageUrls(new ArrayList<>(iphoneProduct.getGalleryImageUrls()));

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));
        when(productRepository.save(any(Product.class))).thenReturn(iphoneProduct);

        productService.updateProduct(updatedProductRequest);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product capturedProduct = productCaptor.getValue();

        assertAll(
                () -> assertEquals(updatedProductRequest.productTitle(), capturedProduct.getProductTitle(), "Product title should be updated"),
                () -> assertEquals(updatedProductRequest.mercadoLivreUrl(), capturedProduct.getMercadoLivreUrl(), "Mercado Livre URL should be updated"),
                () -> assertEquals(updatedProductRequest.mercadoLivreId(), capturedProduct.getMercadoLivreId(), "Mercado Livre ID should be updated"),
                () -> assertEquals(updatedProductRequest.fullDescription(), capturedProduct.getFullDescription(), "Full description should be updated"),
                () -> assertEquals(updatedProductRequest.productBrand(), capturedProduct.getProductBrand(), "Product brand should be updated"),
                () -> assertEquals(updatedProductRequest.productCondition(), capturedProduct.getProductCondition(), "Product condition should be updated"),
                () -> assertEquals(0, updatedProductRequest.currentPrice().compareTo(capturedProduct.getCurrentPrice()), "Current price should be updated"),
                () -> assertEquals(0, updatedProductRequest.originalPrice().compareTo(capturedProduct.getOriginalPrice()), "Original price should be updated"),
                () -> assertEquals(updatedProductRequest.discountPercentage(), capturedProduct.getDiscountPercentage(), "Discount percentage should be updated"),
                () -> assertEquals(updatedProductRequest.installments(), capturedProduct.getInstallments(), "Installments should be updated"),
                () -> assertEquals(0, updatedProductRequest.installmentValue().compareTo(capturedProduct.getInstallmentValue()), "Installment value should be updated"),
                () -> assertEquals(updatedProductRequest.stockStatus(), capturedProduct.getStockStatus(), "Stock status should be updated"),
                () -> assertEquals(updatedProductRequest.affiliateLink(), capturedProduct.getAffiliateLink(), "Affiliate link should be updated"),
                () -> assertEquals(updatedProductRequest.productCategory(), capturedProduct.getProductCategory(), "Category should be updated"),
                () -> assertEquals(updatedProductRequest.productSubcategory(), capturedProduct.getProductSubcategory(), "Subcategory should be updated"),
                () -> assertFalse(capturedProduct.getIsPromotional(), "The product should not be promotional by default")
        );
    }

    @Test
    @DisplayName("Should not update product fields when update request has null values")
    void shouldNotUpdateProductFieldsWhenUpdateRequestHasNullValues() {
        UUID productId = iphoneProduct.getProductId();
        String originalTitle = iphoneProduct.getProductTitle();
        BigDecimal originalPrice = iphoneProduct.getCurrentPrice();

        UpdateProductRequest emptyUpdateRequest = new UpdateProductRequest(
                productId,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.updateProduct(emptyUpdateRequest);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertAll(
                () -> assertEquals(originalTitle, capturedProduct.getProductTitle(), "Title should not have changed"),
                () -> assertEquals(0, originalPrice.compareTo(capturedProduct.getCurrentPrice()), "Price should not have changed"),
                () -> assertNotNull(capturedProduct.getFullDescription(), "Description should not have become null"),
                () -> assertNotNull(capturedProduct.getProductBrand(), "Brand should not have become null")
        );

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when trying to update a non-existent product")
    void shouldThrowProductNotFoundExceptionWhenTryingToUpdateNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();

        UpdateProductRequest updateRequest = new UpdateProductRequest(
                nonExistentProductId,
                "Updated Product", "https://new-url.com", "ML-UPDATED",
                "Updated description", "Brand", "New",
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(120.00),
                "16% OFF", 3, BigDecimal.valueOf(33.33),
                List.of("https://image1.com"), "in stock",
                "https://affiliate-link.com", "Electronics", "Smartphones"
        );

        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(updateRequest));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        UUID productId = iphoneProduct.getProductId();

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when trying to delete a non-existent product")
    void shouldThrowProductNotFoundExceptionWhenTryingToDeleteNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();

        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(nonExistentProductId));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("Should return a list of distinct categories successfully")
    void shouldReturnDistinctCategoriesSuccessfully() {
        List<String> expectedCategories = List.of("Electronics", "Books", "Home Appliances");

        when(productRepository.findDistinctProductCategories()).thenReturn(expectedCategories);

        List<String> actualCategories = productService.getDistinctCategories();

        assertNotNull(actualCategories, "The returned list should not be null.");
        assertEquals(3, actualCategories.size(), "The list should contain 3 categories.");
        assertEquals(expectedCategories, actualCategories, "The returned list should match the expected categories.");
        assertTrue(actualCategories.contains("Electronics"), "The list should contain 'Electronics'.");

        verify(productRepository, times(1)).findDistinctProductCategories();
    }

    @Test
    @DisplayName("Should return an empty list when no distinct categories are found")
    void shouldReturnEmptyListWhenNoCategoriesAreFound() {
        when(productRepository.findDistinctProductCategories()).thenReturn(List.of());

        List<String> actualCategories = productService.getDistinctCategories();

        assertNotNull(actualCategories, "The returned list should not be null, even if empty.");
        assertTrue(actualCategories.isEmpty(), "The list should be empty when no categories are found.");

        verify(productRepository, times(1)).findDistinctProductCategories();
    }

    @Test
    @DisplayName("Should update promotional status of a product successfully")
    void shouldUpdatePromotionalStatusSuccessfully() {
        UUID productId = iphoneProduct.getProductId();
        Boolean newPromotionalStatus = true;

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));
        when(productRepository.save(any(Product.class))).thenReturn(iphoneProduct);

        productService.updatePromotionalStatus(productId, newPromotionalStatus);
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(productArgumentCaptor.capture());

        Product updatedProduct = productArgumentCaptor.getValue();

        assertTrue(updatedProduct.getIsPromotional(), "The product should be marked as promotional.");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when trying to update promotional status of a non-existent product")
    void shouldThrowProductNotFoundExceptionWhenUpdatingPromotionalStatusOfNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();
        Boolean newPromotionalStatus = true;

        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updatePromotionalStatus(nonExistentProductId, newPromotionalStatus));

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return all promotional products successfully")
    void shouldReturnAllPromotionalProductsSuccessfully() {
        iphoneProduct.setIsPromotional(true);
        samsungProduct.setIsPromotional(true);
        List<Product> promotionalProducts = List.of(iphoneProduct, samsungProduct);

        when(productRepository.findAllByIsPromotionalTrue()).thenReturn(promotionalProducts);

        List<ProductDetailsResponse> result = productService.findAllPromotionalProducts();

        verify(productRepository, times(1)).findAllByIsPromotionalTrue();

        assertNotNull(result, "The result list should not be null");
        assertEquals(2, result.size(), "The result list should contain 2 promotional products");
        assertEquals(iphoneProduct.getProductId(), result.get(0).productId(), "First product ID should match iPhone product ID");
        assertEquals(samsungProduct.getProductId(), result.get(1).productId(), "Second product ID should match Samsung product ID");
    }

    @Test
    @DisplayName("Should return an empty list when no promotional products exist")
    void shouldReturnEmptyListWhenNoPromotionalProductsExist() {
        when(productRepository.findAllByIsPromotionalTrue()).thenReturn(List.of());

        List<ProductDetailsResponse> result = productService.findAllPromotionalProducts();

        verify(productRepository, times(1)).findAllByIsPromotionalTrue();

        assertNotNull(result, "The result list should not be null");
        assertTrue(result.isEmpty(), "The result list should be empty when no promotional products exist");
    }

    @Test
    @DisplayName("Should set a new main product image successfully")
    void shouldSetNewMainProductImageSuccessfully() {
        ProductGalleryImageUrl image1 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/main.jpg", null);
        ProductGalleryImageUrl image2 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/secondary.jpg", null);
        List<ProductGalleryImageUrl> gallery = new ArrayList<>(List.of(image1, image2));
        iphoneProduct.setGalleryImageUrls(gallery);

        UUID productId = iphoneProduct.getProductId();
        String newMainImageUrl = "https://image.com/secondary.jpg";

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        productService.setMainProductImage(productId, newMainImageUrl);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertNotNull(savedProduct.getGalleryImageUrls());
        assertEquals(2, savedProduct.getGalleryImageUrls().size());
        assertEquals(newMainImageUrl, savedProduct.getGalleryImageUrls().get(0).getImageUrl(), "The new main image should now be at the first position");
        assertEquals("https://image.com/main.jpg", savedProduct.getGalleryImageUrls().get(1).getImageUrl(), "The old main image should be at the second position");

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when setting main image for a non-existent product")
    void shouldThrowExceptionWhenSettingMainImageForNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();
        String imageUrl = "https://image.com/any.jpg";

        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.setMainProductImage(nonExistentProductId, imageUrl);
        });

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when the image URL is not in the gallery")
    void shouldThrowExceptionWhenImageUrlIsNotInGallery() {
        UUID productId = iphoneProduct.getProductId();
        String nonExistentImageUrl = "https://image.com/non-existent-image.jpg";

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        assertThrows(ProductNotFoundException.class, () -> {
            productService.setMainProductImage(productId, nonExistentImageUrl);
        });

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should do nothing if the image is already the main one")
    void shouldDoNothingIfImageIsAlreadyTheMainOne() {
        ProductGalleryImageUrl image1 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/main.jpg", null);
        iphoneProduct.setGalleryImageUrls(new ArrayList<>(List.of(image1)));

        UUID productId = iphoneProduct.getProductId();
        String alreadyMainImageUrl = "https://image.com/main.jpg";
        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        productService.setMainProductImage(productId, alreadyMainImageUrl);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product gallery image successfully")
    void shouldDeleteProductGalleryImageSuccessfully() {
        ProductGalleryImageUrl image1 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/image1.jpg", null);
        ProductGalleryImageUrl image2 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/image2.jpg", null);
        iphoneProduct.setGalleryImageUrls(new ArrayList<>(List.of(image1, image2)));

        UUID productId = iphoneProduct.getProductId();
        String imageToDelete = "https://image.com/image1.jpg";

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        productService.deleteProductImage(productId, imageToDelete);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productCaptor.capture());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));

        Product savedProduct = productCaptor.getValue();

        assertNotNull(savedProduct.getGalleryImageUrls());
        assertEquals(1, savedProduct.getGalleryImageUrls().size());
        assertEquals(image2.getImageUrl(), savedProduct.getGalleryImageUrls().get(0).getImageUrl(), "The remaining image should be the second one");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting an image from a non-existent product")
    void shouldThrowExceptionWhenDeletingImageFromNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();
        String imageUrl = "https://qualquer-imagem.com";

        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProductImage(nonExistentProductId, imageUrl);
        });

        verify(productRepository, times(1)).findById(nonExistentProductId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when image URL to delete is not in the gallery")
    void shouldThrowExceptionWhenImageUrlToDeleteIsNotInGallery() {
        ProductGalleryImageUrl image1 = new ProductGalleryImageUrl(UUID.randomUUID(), "https://image.com/image1.jpg", null);
        iphoneProduct.setGalleryImageUrls(new ArrayList<>(List.of(image1)));

        UUID productId = iphoneProduct.getProductId();
        String nonExistentImageUrl = "https://url-que-nao-existe.com";

        when(productRepository.findById(productId)).thenReturn(Optional.of(iphoneProduct));

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProductImage(productId, nonExistentImageUrl);
        });

        assertTrue(exception.getMessage().contains("not found in product gallery"));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }
}