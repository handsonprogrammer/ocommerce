package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Product summary response DTO for list views
 */
@Schema(description = "Product summary information for list views")
public class ProductSummaryResponse {

    @Schema(description = "Product unique identifier")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Product name", example = "MacBook Pro 16-inch")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Short product description")
    @JsonProperty("shortDescription")
    private String shortDescription;

    @Schema(description = "Product thumbnail image URL")
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    @Schema(description = "Base price", example = "2499.99")
    @JsonProperty("basePrice")
    private Double basePrice;

    @Schema(description = "Price range for variants")
    @JsonProperty("priceRange")
    private PriceRangeResponse priceRange;

    @Schema(description = "Category IDs")
    @JsonProperty("categoryIds")
    private List<UUID> categoryIds;

    @Schema(description = "Category hierarchy paths", example = "[\"Electronics>Computers>Laptops\", \"Featured>New Arrivals\"]")
    @JsonProperty("categoryPaths")
    private List<String> categoryPaths;

    @Schema(description = "Product status", example = "ACTIVE")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Number of variants", example = "3")
    @JsonProperty("variantCount")
    private int variantCount;

    @Schema(description = "Whether product is in stock", example = "true")
    @JsonProperty("inStock")
    private boolean inStock;

    @Schema(description = "SEO slug", example = "macbook-pro-16-inch")
    @JsonProperty("slug")
    private String slug;

    // Constructors
    public ProductSummaryResponse() {}

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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public PriceRangeResponse getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(PriceRangeResponse priceRange) {
        this.priceRange = priceRange;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(int variantCount) {
        this.variantCount = variantCount;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Price range response for products with variants
     */
    public static class PriceRangeResponse {
        @Schema(description = "Minimum price", example = "2299.99")
        @JsonProperty("minPrice")
        private Double minPrice;

        @Schema(description = "Maximum price", example = "2999.99")
        @JsonProperty("maxPrice")
        private Double maxPrice;

        // Constructors, getters, and setters
        public PriceRangeResponse() {}

        public PriceRangeResponse(Double minPrice, Double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }

        public Double getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(Double minPrice) {
            this.minPrice = minPrice;
        }

        public Double getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
        }
    }
}
