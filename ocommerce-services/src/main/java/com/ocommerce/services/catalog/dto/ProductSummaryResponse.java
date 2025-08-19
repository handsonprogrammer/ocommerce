package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Product summary response DTO for list views
 */
@Schema(description = "Product summary information for list views")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Schema(description = "Whether product is in stock", example = "true")
    @JsonProperty("inStock")
    private boolean inStock;

    @Schema(description = "Number of variants available", example = "3")
    @JsonProperty("variantCount")
    private int variantCount;

    /**
     * Price range response DTO
     */
    @Schema(description = "Price range information")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRangeResponse {
        @Schema(description = "Minimum price", example = "1999.99")
        @JsonProperty("minPrice")
        private Double minPrice;

        @Schema(description = "Maximum price", example = "2999.99")
        @JsonProperty("maxPrice")
        private Double maxPrice;

        @Schema(description = "Whether all variants have same price", example = "false")
        @JsonProperty("hasSinglePrice")
        private boolean hasSinglePrice;

        public PriceRangeResponse(Double minPrice, Double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.hasSinglePrice = (minPrice != null && maxPrice != null && minPrice.equals(maxPrice));
        }
    }
}
