package com.ocommerce.services.catalog.controller;

import com.ocommerce.services.catalog.dto.ProductResponse;
import com.ocommerce.services.catalog.dto.ProductSummaryResponse;
import com.ocommerce.services.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for product management endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Product catalog operations")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products with pagination and sorting
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all active products with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<Page<ProductSummaryResponse>> getAllProducts(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/products - Get all products (page: {}, size: {}, sort: {} {})",
                   page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductSummaryResponse> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable UUID id) {
        log.info("GET /api/v1/products/{} - Get product by ID", id);

        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get product by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieve a product by its SEO-friendly slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Product slug", required = true, example = "macbook-pro-16-inch")
            @PathVariable String slug) {
        log.info("GET /api/v1/products/slug/{} - Get product by slug", slug);

        return productService.getProductBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by text with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product search completed",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<Page<ProductSummaryResponse>> searchProducts(
            @Parameter(description = "Search query", required = true, example = "laptop")
            @RequestParam String q,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/products/search?q={} - Search products", q);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductSummaryResponse> products = productService.searchProducts(q, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     */
    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve products belonging to a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<Page<ProductSummaryResponse>> getProductsByCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/products/categories/{} - Get products by category", categoryId);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductSummaryResponse> products = productService.getProductsByCategory(categoryId, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Advanced product search with filters
     */
    @GetMapping("/search/advanced")
    @Operation(summary = "Advanced product search", description = "Search products with multiple filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Advanced search completed",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<Page<ProductSummaryResponse>> advancedSearch(
            @Parameter(description = "Search text", example = "laptop")
            @RequestParam(required = false) String q,
            @Parameter(description = "Category IDs (comma-separated)", example = "uuid1,uuid2")
            @RequestParam(required = false) List<UUID> categories,
            @Parameter(description = "Minimum price", example = "100.00")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price", example = "5000.00")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "basePrice")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/products/search/advanced - Advanced search: q={}, categories={}, price={}-{}",
                   q, categories, minPrice, maxPrice);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Set default price range if not provided
        if (minPrice == null) minPrice = Double.valueOf("0.00");
        if (maxPrice == null) maxPrice = 999999.99;

        Page<ProductSummaryResponse> products = productService.complexSearch(
                q != null ? q : "",
                categories != null ? categories : List.of(),
                minPrice, maxPrice, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Get product by variant SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a product by its variant SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product variant SKU", required = true, example = "MBP16-SG-512")
            @PathVariable String sku) {
        log.info("GET /api/v1/products/sku/{} - Get product by SKU", sku);

        return productService.getProductByVariantSku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get products with low stock
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock", description = "Retrieve products that are running low on stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<List<ProductSummaryResponse>> getProductsWithLowStock() {
        log.info("GET /api/v1/products/low-stock - Get products with low stock");

        List<ProductSummaryResponse> products = productService.getProductsWithLowStock();
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by price range
     */
    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Retrieve products within a specific price range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    })
    public ResponseEntity<Page<ProductSummaryResponse>> getProductsByPriceRange(
            @Parameter(description = "Minimum price", required = true, example = "100.00")
            @RequestParam Double minPrice,
            @Parameter(description = "Maximum price", required = true, example = "1000.00")
            @RequestParam Double maxPrice,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "basePrice")
            @RequestParam(defaultValue = "basePrice") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/v1/products/price-range - Get products by price range: {}-{}", minPrice, maxPrice);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductSummaryResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);

        return ResponseEntity.ok(products);
    }
}
