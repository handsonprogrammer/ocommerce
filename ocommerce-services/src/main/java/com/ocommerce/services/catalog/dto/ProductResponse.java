package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product response DTO for API responses
 */
@Schema(description = "Product information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Product name", example = "MacBook Pro 16-inch")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Short product description", example = "Powerful laptop for professionals")
    @JsonProperty("shortDescription")
    private String shortDescription;

    @Schema(description = "Detailed product description")
    @JsonProperty("longDescription")
    private String longDescription;

    @Schema(description = "Product thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Product image URLs")
    @JsonProperty("imageUrls")
    private List<String> imageUrls;

    @Schema(description = "Base price", example = "2499.99")
    @JsonProperty("basePrice")
    private Double basePrice;

    @Schema(description = "Unit of measure", example = "piece")
    @JsonProperty("unitOfMeasure")
    private String unitOfMeasure;

    @Schema(description = "Category IDs this product belongs to")
    @JsonProperty("categoryIds")
    private List<UUID> categoryIds;

    @Schema(description = "Category hierarchy paths", example = "[\"Electronics>Computers>Laptops\", \"Featured>New Arrivals\"]")
    @JsonProperty("categoryPaths")
    private List<String> categoryPaths;

    @Schema(description = "Product variants")
    @JsonProperty("variants")
    private List<ProductVariantResponse> variants;

    @Schema(description = "SEO metadata")
    @JsonProperty("seoMetadata")
    private CategoryResponse.SeoMetadataResponse seoMetadata;

    @Schema(description = "Product attributes")
    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    @Schema(description = "Product status", example = "ACTIVE")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Whether inventory is tracked", example = "true")
    @JsonProperty("inventoryTracking")
    private boolean inventoryTracking;

    @Schema(description = "Product weight", example = "2.1")
    @JsonProperty("weight")
    private Double weight;

    @Schema(description = "Product dimensions")
    @JsonProperty("dimensions")
    private ProductDimensionsResponse dimensions;

    @Schema(description = "Product creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Product last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Product dimensions response DTO
     */
    @Schema(description = "Product dimensions information")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDimensionsResponse {
        @Schema(description = "Length", example = "35.0")
        @JsonProperty("length")
        private Double length;

        @Schema(description = "Width", example = "24.0")
        @JsonProperty("width")
        private Double width;

        @Schema(description = "Height", example = "1.6")
        @JsonProperty("height")
        private Double height;

        @Schema(description = "Unit of measurement", example = "cm")
        @JsonProperty("unit")
        private String unit;
    }
}
