package com.ocommerce.services.catalog.service;

import com.ocommerce.services.catalog.constants.ProductStatus;
import com.ocommerce.services.catalog.domain.Category;
import com.ocommerce.services.catalog.domain.Product;
import com.ocommerce.services.catalog.domain.ProductVariant;
import com.ocommerce.services.catalog.dto.CategoryResponse;
import com.ocommerce.services.catalog.dto.ProductResponse;
import com.ocommerce.services.catalog.dto.ProductSummaryResponse;
import com.ocommerce.services.catalog.dto.ProductVariantResponse;
import com.ocommerce.services.catalog.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ExecutableFindOperation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Service class for Product domain operations
 */
@Slf4j
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryPathService categoryPathService;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryPathService categoryPathService) {
        this.productRepository = productRepository;
        this.categoryPathService = categoryPathService;
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(UUID productId) {
        log.info("Fetching product with ID: {}", productId);
        return productRepository.findById(productId)
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .map(this::convertToProductResponse);
    }

    /**
     * Get product by slug
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySlug(String slug) {
        log.info("Fetching product with slug: {}", slug);
        return productRepository.findBySlugAndActive(slug)
                .map(this::convertToProductResponse);
    }

    /**
     * Get all active products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all active products with pagination: {}", pageable);
        Page<Product> products = productRepository.findByStatusAndInventoryTrackingTrue(
                ProductStatus.ACTIVE, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Get products by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        log.info("Fetching products for category ID: {} with pagination: {}", categoryId, pageable);
        Page<Product> products = productRepository.findByCategoryIdAndActive(categoryId, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Get products by multiple categories
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProductsByCategories(List<UUID> categoryIds, Pageable pageable) {
        log.info("Fetching products for category IDs: {} with pagination: {}", categoryIds, pageable);
        Page<Product> products = productRepository.findByCategoryIdsInAndActive(categoryIds, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Search products by text
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProducts(String searchText, Pageable pageable) {
        log.info("Searching products with text: {} and pagination: {}", searchText, pageable);
        Page<Product> products = productRepository.findByTextSearchAndActive(searchText, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Search products by name
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProductsByName(String name, Pageable pageable) {
        log.info("Searching products by name: {} with pagination: {}", name, pageable);
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseAndActive(name, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        log.info("Fetching products with price range: {} - {} with pagination: {}", minPrice, maxPrice, pageable);
        Page<Product> products = productRepository.findByBasePriceBetweenAndStatus(minPrice, maxPrice, ProductStatus.ACTIVE, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Complex search with multiple filters
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> complexSearch(String searchText, List<UUID> categoryIds,
                                                    Double minPrice, Double maxPrice, Pageable pageable) {
        log.info("Complex search - text: {}, categories: {}, price: {}-{}",
                   searchText, categoryIds, minPrice, maxPrice);

        Page<Product> products = productRepository.findByComplexSearch(
                searchText, categoryIds, minPrice, maxPrice, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Get product by variant SKU
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductByVariantSku(String sku) {
        log.info("Fetching product with variant SKU: {}", sku);
        return productRepository.findByVariantSkuAndActive(sku)
                .map(this::convertToProductResponse);
    }

    /**
     * Count products by category
     */
    @Transactional(readOnly = true)
    public long countProductsByCategory(UUID categoryId) {
        return productRepository.countByCategoryIdAndActive(categoryId);
    }

    /**
     * Get products with low stock
     */
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getProductsWithLowStock() {
        log.info("Fetching products with low stock");
        List<Product> products = productRepository.findProductsWithLowStock();
        return products.stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new product with automatic category path generation
     */
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());

        // Generate category paths automatically
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            List<String> categoryPaths = categoryPathService.generateCategoryPaths(product.getCategories());
            product.setCategoryPaths(categoryPaths);
            log.debug("Generated category paths for product {}: {}", product.getName(), categoryPaths);
        }

        return productRepository.save(product);
    }

    /**
     * Update an existing product with automatic category path regeneration
     */
    public Product updateProduct(Product product) {
        log.info("Updating product: {}", product.getId());

        // Regenerate category paths when updating
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            List<String> categoryPaths = categoryPathService.generateCategoryPaths(product.getCategories());
            product.setCategoryPaths(categoryPaths);
            log.debug("Regenerated category paths for product {}: {}", product.getId(), categoryPaths);
        } else {
            // Clear category paths if no categories are assigned
            product.setCategoryPaths(null);
        }

        return productRepository.save(product);
    }

    /**
     * Get products by category hierarchy path
     */
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getProductsByCategoryPath(String categoryPath, Pageable pageable) {
        log.info("Fetching products for category path: {} with pagination: {}", categoryPath, pageable);
        Page<Product> products = productRepository.findByCategoryPathContaining(categoryPath, pageable);

        List<ProductSummaryResponse> summaryResponses = products.getContent().stream()
                .map(this::convertToProductSummaryResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryResponses, pageable, products.getTotalElements());
    }

    /**
     * Update category paths for products when categories are modified
     */
    public void updateCategoryPathsForCategory(UUID categoryId) {
        log.info("Updating category paths for all products in category: {}", categoryId);

        // Find all products that belong to this category
        List<Product> products = productRepository.findByCategoryId(categoryId);

        for (Product product : products) {
            if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                List<String> updatedPaths = categoryPathService.generateCategoryPaths(product.getCategories());
                product.setCategoryPaths(updatedPaths);
                productRepository.save(product);
                log.debug("Updated category paths for product {}: {}", product.getId(), updatedPaths);
            }
        }

        log.info("Completed updating category paths for {} products", products.size());
    }

    /**
     * Bulk update category paths for all products
     */
    public void regenerateAllCategoryPaths() {
        log.info("Starting bulk regeneration of category paths for all products");

        List<Product> allProducts = productRepository.findAll();
        int updatedCount = 0;

        for (Product product : allProducts) {
            if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                List<String> categoryPaths = categoryPathService.generateCategoryPaths(product.getCategories());
                product.setCategoryPaths(categoryPaths);
                productRepository.save(product);
                updatedCount++;

                if (updatedCount % 100 == 0) {
                    log.info("Updated category paths for {} products", updatedCount);
                }
            }
        }

        log.info("Completed bulk regeneration of category paths for {} products", updatedCount);
    }

    // Private helper methods

    /**
     * Convert Product entity to ProductResponse DTO
     */
    private ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setShortDescription(product.getShortDescription());
        response.setLongDescription(product.getLongDescription());
        response.setThumbnailUrl(product.getThumbnailUrl());
        response.setImageUrls(product.getImageUrls());
        response.setBasePrice(product.getBasePrice());
        response.setUnitOfMeasure(product.getUnitOfMeasure());
        response.setCategoryIds(
                product.getCategories() != null && !product.getCategories().isEmpty()
                        ? product.getCategories().stream().map(Category::getId).collect(Collectors.toList())
                        : List.of()
        );
        response.setCategoryPaths(product.getCategoryPaths());
        response.setAttributes(product.getAttributes());
        response.setStatus(product.getStatus().name());
        response.setInventoryTracking(product.isInventoryTracking());
        response.setWeight(product.getWeight());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Convert dimensions
        if (product.getDimensions() != null) {
            ProductResponse.ProductDimensionsResponse dimensionsResponse =
                new ProductResponse.ProductDimensionsResponse();
            dimensionsResponse.setLength(product.getDimensions().getLength());
            dimensionsResponse.setWidth(product.getDimensions().getWidth());
            dimensionsResponse.setHeight(product.getDimensions().getHeight());
            dimensionsResponse.setUnit(product.getDimensions().getUnit());
            response.setDimensions(dimensionsResponse);
        }

        // Convert SEO metadata
        if (product.getSeoMetadata() != null) {
            CategoryResponse.SeoMetadataResponse seoResponse = new CategoryResponse.SeoMetadataResponse();
            seoResponse.setMetaTitle(product.getSeoMetadata().getMetaTitle());
            seoResponse.setMetaDescription(product.getSeoMetadata().getMetaDescription());
            seoResponse.setMetaKeywords(product.getSeoMetadata().getMetaKeywords());
            seoResponse.setSlug(product.getSeoMetadata().getSlug());
            seoResponse.setCanonicalUrl(product.getSeoMetadata().getCanonicalUrl());
            response.setSeoMetadata(seoResponse);
        }

        // Convert variants
        if (product.getVariants() != null) {
            List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                    .map(this::convertToProductVariantResponse)
                    .collect(Collectors.toList());
            response.setVariants(variantResponses);
        }

        return response;
    }

    /**
     * Convert Product entity to ProductSummaryResponse DTO
     */
    private ProductSummaryResponse convertToProductSummaryResponse(Product product) {
        ProductSummaryResponse response = new ProductSummaryResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setShortDescription(product.getShortDescription());
        response.setThumbnailUrl(product.getThumbnailUrl());
        response.setBasePrice(product.getBasePrice());
        response.setCategoryIds(
                product.getCategories() != null && !product.getCategories().isEmpty()
                        ? product.getCategories().stream().map(Category::getId).collect(Collectors.toList())
                        : null
        );
        response.setStatus(product.getStatus().name());
        response.setVariantCount(product.getVariants() != null ? product.getVariants().size() : 0);
        response.setCategoryPaths(product.getCategoryPaths());


        // Calculate price range from variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            Double minPrice = product.getVariants().stream()
                    .map(ProductVariant::getPrice)
                    .filter(price -> price != null)
                    .min(Double::compareTo)
                    .orElse(product.getBasePrice());

            Double maxPrice = product.getVariants().stream()
                    .map(ProductVariant::getPrice)
                    .filter(price -> price != null)
                    .max(Double::compareTo)
                    .orElse(product.getBasePrice());

            response.setPriceRange(new ProductSummaryResponse.PriceRangeResponse(minPrice, maxPrice));
        }

        // Check if product is in stock
        boolean inStock = product.getVariants() != null &&
                         product.getVariants().stream()
                                 .anyMatch(variant -> variant.getInventory() != null &&
                                          variant.getInventory().isInStock());
        response.setInStock(inStock);

        return response;
    }

    /**
     * Convert ProductVariant entity to ProductVariantResponse DTO
     */
    private ProductVariantResponse convertToProductVariantResponse(ProductVariant variant) {
        ProductVariantResponse response = new ProductVariantResponse();
        response.setVariantId(variant.getVariantId());
        response.setSku(variant.getSku());
        response.setBarcode(variant.getBarcode());
        response.setVariantName(variant.getVariantName());
        response.setPrice(variant.getPrice());
        response.setCompareAtPrice(variant.getCompareAtPrice());
        response.setAttributes(variant.getAttributes());
        response.setImageUrls(variant.getImageUrls());
        response.setWeight(variant.getWeight());
        response.setStatus(variant.getStatus().name());
        response.setPosition(variant.getPosition());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());

        // Convert dimensions
        if (variant.getDimensions() != null) {
            ProductResponse.ProductDimensionsResponse dimensionsResponse =
                new ProductResponse.ProductDimensionsResponse();
            dimensionsResponse.setLength(variant.getDimensions().getLength());
            dimensionsResponse.setWidth(variant.getDimensions().getWidth());
            dimensionsResponse.setHeight(variant.getDimensions().getHeight());
            dimensionsResponse.setUnit(variant.getDimensions().getUnit());
            response.setDimensions(dimensionsResponse);
        }

        // Convert inventory
        if (variant.getInventory() != null) {
            ProductVariantResponse.VariantInventoryResponse inventoryResponse =
                new ProductVariantResponse.VariantInventoryResponse();
            inventoryResponse.setQuantity(variant.getInventory().getQuantity());
            inventoryResponse.setReservedQuantity(variant.getInventory().getReservedQuantity());
            inventoryResponse.setAvailableQuantity(variant.getInventory().getAvailableQuantity());
            inventoryResponse.setLowStockThreshold(variant.getInventory().getLowStockThreshold());
            inventoryResponse.setTrackInventory(variant.getInventory().isTrackInventory());
            inventoryResponse.setAllowBackorder(variant.getInventory().isAllowBackorder());
            inventoryResponse.setInventoryPolicy(variant.getInventory().getInventoryPolicy().name());
            inventoryResponse.setInStock(variant.getInventory().isInStock());
            inventoryResponse.setLowStock(variant.getInventory().isLowStock());
            response.setInventory(inventoryResponse);
        }

        return response;
    }
}
