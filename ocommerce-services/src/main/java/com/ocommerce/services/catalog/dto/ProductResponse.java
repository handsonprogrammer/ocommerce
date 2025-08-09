package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product response DTO for API responses
 */
@Schema(description = "Product information")
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

    // Constructors
    public ProductResponse() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<String> getCategoryPaths() {
        return categoryPaths;
    }

    public void setCategoryPaths(List<String> categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    public List<ProductVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantResponse> variants) {
        this.variants = variants;
    }

    public CategoryResponse.SeoMetadataResponse getSeoMetadata() {
        return seoMetadata;
    }

    public void setSeoMetadata(CategoryResponse.SeoMetadataResponse seoMetadata) {
        this.seoMetadata = seoMetadata;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isInventoryTracking() {
        return inventoryTracking;
    }

    public void setInventoryTracking(boolean inventoryTracking) {
        this.inventoryTracking = inventoryTracking;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ProductDimensionsResponse getDimensions() {
        return dimensions;
    }

    public void setDimensions(ProductDimensionsResponse dimensions) {
        this.dimensions = dimensions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Product dimensions response DTO
     */
    public static class ProductDimensionsResponse {
        @Schema(description = "Length", example = "35.79")
        @JsonProperty("length")
        private Double length;

        @Schema(description = "Width", example = "24.59")
        @JsonProperty("width")
        private Double width;

        @Schema(description = "Height", example = "1.62")
        @JsonProperty("height")
        private Double height;

        @Schema(description = "Dimension unit", example = "cm")
        @JsonProperty("unit")
        private String unit;

        // Constructors, getters, and setters
        public ProductDimensionsResponse() {}

        public Double getLength() {
            return length;
        }

        public void setLength(Double length) {
            this.length = length;
        }

        public Double getWidth() {
            return width;
        }

        public void setWidth(Double width) {
            this.width = width;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
