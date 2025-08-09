package com.ocommerce.services.catalog.service;

import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Product;
import com.ocommerce.services.catalog.domain.ProductVariant;
import com.ocommerce.services.catalog.dto.ProductResponse;
import com.ocommerce.services.catalog.dto.ProductSummaryResponse;
import com.ocommerce.services.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductVariant testVariant;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        testVariant = createTestVariant();
        testProduct.setVariants(List.of(testVariant));
        testPageable = PageRequest.of(0, 20);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        UUID productId = testProduct.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Optional<ProductResponse> result = productService.getProductById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(productId);
        assertThat(result.get().getName()).isEqualTo("MacBook Pro 16-inch");
        assertThat(result.get().getVariants()).hasSize(1);
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        // Given
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Optional<ProductResponse> result = productService.getProductById(productId);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductBySlug_WhenProductExists_ShouldReturnProduct() {
        // Given
        String slug = "macbook-pro-16-inch";
        when(productRepository.findBySlugAndActive(slug)).thenReturn(Optional.of(testProduct));

        // When
        Optional<ProductResponse> result = productService.getProductBySlug(slug);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findBySlugAndActive(slug);
    }

    @Test
    void getAllProducts_ShouldReturnPaginatedProducts() {
        // Given
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByStatusAndInventoryTrackingTrue(ProductStatus.ACTIVE, testPageable))
                .thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.getAllProducts(testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByStatusAndInventoryTrackingTrue(ProductStatus.ACTIVE, testPageable);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByCategoryIdAndActive(categoryId, testPageable)).thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.getProductsByCategory(categoryId, testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByCategoryIdAndActive(categoryId, testPageable);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Given
        String searchText = "laptop";
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByTextSearchAndActive(searchText, testPageable)).thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.searchProducts(searchText, testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByTextSearchAndActive(searchText, testPageable);
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        // Given
        String name = "MacBook";
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByNameContainingIgnoreCaseAndActive(name, testPageable)).thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.searchProductsByName(name, testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByNameContainingIgnoreCaseAndActive(name, testPageable);
    }

    @Test
    void getProductsByPriceRange_ShouldReturnProductsInRange() {
        // Given
        Double minPrice = new Double("1000.00");
        Double maxPrice = new Double("3000.00");
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByBasePriceBetweenAndStatus(minPrice, maxPrice, ProductStatus.ACTIVE, testPageable)).thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.getProductsByPriceRange(minPrice, maxPrice, testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBasePrice()).isEqualTo(new Double("2499.99"));
        verify(productRepository).findByBasePriceBetweenAndStatus(minPrice, maxPrice, ProductStatus.ACTIVE, testPageable);
    }

    @Test
    void complexSearch_ShouldReturnMatchingProducts() {
        // Given
        String searchText = "laptop";
        List<UUID> categoryIds = List.of(UUID.randomUUID());
        Double minPrice = new Double("1000.00");
        Double maxPrice = new Double("3000.00");
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);

        when(productRepository.findByComplexSearch(searchText, categoryIds, minPrice, maxPrice, testPageable))
                .thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.complexSearch(searchText, categoryIds, minPrice, maxPrice, testPageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByComplexSearch(searchText, categoryIds, minPrice, maxPrice, testPageable);
    }

    @Test
    void getProductByVariantSku_WhenProductExists_ShouldReturnProduct() {
        // Given
        String sku = "MBP16-SG-512";
        when(productRepository.findByVariantSkuAndActive(sku)).thenReturn(Optional.of(testProduct));

        // When
        Optional<ProductResponse> result = productService.getProductByVariantSku(sku);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findByVariantSkuAndActive(sku);
    }

    @Test
    void countProductsByCategory_ShouldReturnCount() {
        // Given
        UUID categoryId = UUID.randomUUID();
        long expectedCount = 25L;
        when(productRepository.countByCategoryIdAndActive(categoryId)).thenReturn(expectedCount);

        // When
        long result = productService.countProductsByCategory(categoryId);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(productRepository).countByCategoryIdAndActive(categoryId);
    }

    @Test
    void getProductsWithLowStock_ShouldReturnLowStockProducts() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findProductsWithLowStock()).thenReturn(products);

        // When
        List<ProductSummaryResponse> result = productService.getProductsWithLowStock();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MacBook Pro 16-inch");
        verify(productRepository).findProductsWithLowStock();
    }

    @Test
    void convertToProductSummaryResponse_ShouldCalculatePriceRange() {
        // Given - testProduct with variant already set up in setUp()
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, testPageable, 1);
        when(productRepository.findByStatusAndInventoryTrackingTrue(
                ProductStatus.ACTIVE, testPageable)).thenReturn(productPage);

        // When
        Page<ProductSummaryResponse> result = productService.getAllProducts(testPageable);

        // Then
        ProductSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.getPriceRange()).isNotNull();
        assertThat(summary.getPriceRange().getMinPrice()).isEqualTo(new Double("2499.99"));
        assertThat(summary.getPriceRange().getMaxPrice()).isEqualTo(new Double("2499.99"));
        assertThat(summary.getVariantCount()).isEqualTo(1);
        assertThat(summary.isInStock()).isTrue();
    }

    // Helper methods
    private Product createTestProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("MacBook Pro 16-inch");
        product.setShortDescription("Powerful laptop for professionals");
        product.setLongDescription("The MacBook Pro 16-inch is designed for professionals who need power and performance.");
        product.setThumbnailUrl("https://example.com/macbook-thumb.jpg");
        product.setImageUrls(List.of("https://example.com/macbook1.jpg", "https://example.com/macbook2.jpg"));
        product.setBasePrice(new Double("2499.99"));
        product.setUnitOfMeasure("piece");
        product.setCategoryIds(List.of(UUID.randomUUID()));
        product.setStatus(ProductStatus.ACTIVE);
        product.setInventoryTracking(true);
        product.setWeight(2.1);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Add dimensions
        Product.ProductDimensions dimensions = new Product.ProductDimensions(35.79, 24.59, 1.62, "cm");
        product.setDimensions(dimensions);

        // Add SEO metadata
        Product.SeoMetadata seoMetadata = new Product.SeoMetadata();
        seoMetadata.setSlug("macbook-pro-16-inch");
        seoMetadata.setMetaTitle("MacBook Pro 16-inch - Professional Laptop");
        seoMetadata.setMetaDescription("High-performance laptop for professionals");
        product.setSeoMetadata(seoMetadata);

        return product;
    }

    private ProductVariant createTestVariant() {
        ProductVariant variant = new ProductVariant();
        variant.setVariantId(UUID.randomUUID());
        variant.setSku("MBP16-SG-512");
        variant.setBarcode("1234567890123");
        variant.setVariantName("Space Gray 512GB");
        variant.setPrice(new Double("2499.99"));
        variant.setCompareAtPrice(new BigDecimal("2799.99"));
        variant.setStatus(ProductVariant.VariantStatus.ACTIVE);
        variant.setPosition(1);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        // Add attributes
        variant.setAttributes(java.util.Map.of(
                "color", "Space Gray",
                "storage", "512GB"
        ));

        // Add inventory
        ProductVariant.VariantInventory inventory = new ProductVariant.VariantInventory();
        inventory.setQuantity(100);
        inventory.setReservedQuantity(5);
        inventory.setLowStockThreshold(10);
        inventory.setTrackInventory(true);
        inventory.setAllowBackorder(false);
        variant.setInventory(inventory);

        return variant;
    }
}
