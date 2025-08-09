package com.ocommerce.services.catalog.controller;

import com.ocommerce.services.catalog.dto.CategoryResponse;
import com.ocommerce.services.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for category management endpoints
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "Category catalog operations")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get all root categories
     */
    @GetMapping
    @Operation(summary = "Get all root categories", description = "Retrieve all root categories (categories without parent)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        logger.info("GET /api/v1/categories - Get all root categories");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID id) {
        logger.info("GET /api/v1/categories/{} - Get category by ID", id);

        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get category by slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve a category by its SEO-friendly slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category slug", required = true, example = "electronics")
            @PathVariable String slug) {
        logger.info("GET /api/v1/categories/slug/{} - Get category by slug", slug);

        return categoryService.getCategoryBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get child categories of a parent category
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Retrieve all child categories of a parent category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Child categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getChildCategories(
            @Parameter(description = "Parent category ID", required = true)
            @PathVariable UUID id) {
        logger.info("GET /api/v1/categories/{}/children - Get child categories", id);

        List<CategoryResponse> childCategories = categoryService.getChildCategories(id);
        return ResponseEntity.ok(childCategories);
    }

    /**
     * Get category hierarchy tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Retrieve the complete category hierarchy as a tree structure")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryService.CategoryTreeResponse.class)))
    })
    public ResponseEntity<List<CategoryService.CategoryTreeResponse>> getCategoryTree() {
        logger.info("GET /api/v1/categories/tree - Get category hierarchy tree");

        List<CategoryService.CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    /**
     * Get all categories
     */
    @GetMapping("/all")
    @Operation(summary = "Get all categories", description = "Retrieve all active categories ordered by sort order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        logger.info("GET /api/v1/categories/all - Get all categories");

        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get categories by level
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "Get categories by level", description = "Retrieve categories at a specific hierarchy level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getCategoriesByLevel(
            @Parameter(description = "Hierarchy level (0 for root, 1 for first level, etc.)", required = true)
            @PathVariable Integer level) {
        logger.info("GET /api/v1/categories/level/{} - Get categories by level", level);

        List<CategoryResponse> categories = categoryService.getCategoriesByLevel(level);
        return ResponseEntity.ok(categories);
    }

    /**
     * Search categories by name
     */
    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name (case-insensitive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories search completed",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> searchCategories(
            @Parameter(description = "Search term", required = true, example = "electronics")
            @RequestParam String q) {
        logger.info("GET /api/v1/categories/search?q={} - Search categories", q);

        List<CategoryResponse> categories = categoryService.searchCategoriesByName(q);
        return ResponseEntity.ok(categories);
    }

    /**
     * Full text search across categories
     */
    @GetMapping("/search/text")
    @Operation(summary = "Text search categories", description = "Full text search across category name and description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text search completed",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> textSearchCategories(
            @Parameter(description = "Search text", required = true, example = "electronic devices")
            @RequestParam String text) {
        logger.info("GET /api/v1/categories/search/text?text={} - Text search categories", text);

        List<CategoryResponse> categories = categoryService.textSearchCategories(text);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get categories with minimum product count
     */
    @GetMapping("/with-products")
    @Operation(summary = "Get categories with products", description = "Retrieve categories that have at least the specified number of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories with products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithProducts(
            @Parameter(description = "Minimum product count", example = "1")
            @RequestParam(defaultValue = "1") Long minProducts) {
        logger.info("GET /api/v1/categories/with-products?minProducts={} - Get categories with products", minProducts);

        List<CategoryResponse> categories = categoryService.getCategoriesWithMinProducts(minProducts);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get category statistics", description = "Get basic statistics about categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category statistics retrieved successfully")
    })
    public ResponseEntity<CategoryStats> getCategoryStats() {
        logger.info("GET /api/v1/categories/stats - Get category statistics");

        long activeCategoriesCount = categoryService.getActiveCategoriesCount();
        CategoryStats stats = new CategoryStats(activeCategoriesCount);

        return ResponseEntity.ok(stats);
    }

    /**
     * Inner class for category statistics response
     */
    public static class CategoryStats {
        private final long activeCategoriesCount;
        private final long timestamp;

        public CategoryStats(long activeCategoriesCount) {
            this.activeCategoriesCount = activeCategoriesCount;
            this.timestamp = System.currentTimeMillis();
        }

        public long getActiveCategoriesCount() {
            return activeCategoriesCount;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
