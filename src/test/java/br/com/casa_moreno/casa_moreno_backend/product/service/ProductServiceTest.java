package br.com.casa_moreno.casa_moreno_backend.product.service;

import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperClient;
import br.com.casa_moreno.casa_moreno_backend.client.MercadoLivreScraperResponse;
import br.com.casa_moreno.casa_moreno_backend.exception.ProductAlreadyExistsException;
import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import br.com.casa_moreno.casa_moreno_backend.product.domain.ProductGalleryImageUrl;
import br.com.casa_moreno.casa_moreno_backend.product.dto.CreateProductRequest;
import br.com.casa_moreno.casa_moreno_backend.product.dto.ProductDetailsResponse;
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
}