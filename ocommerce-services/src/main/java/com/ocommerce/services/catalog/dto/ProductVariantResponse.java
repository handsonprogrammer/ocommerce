package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product variant response DTO
 */
@Schema(description = "Product variant information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

    @Schema(description = "Variant unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("variantId")
    private UUID variantId;

    @Schema(description = "SKU (Stock Keeping Unit)", example = "MBP16-SG-512")
    @JsonProperty("sku")
    private String sku;

    @Schema(description = "Barcode", example = "1234567890123")
    @JsonProperty("barcode")
    private String barcode;

    @Schema(description = "Variant name", example = "Space Gray 512GB")
    @JsonProperty("variantName")
    private String variantName;

    @Schema(description = "Variant price", example = "2499.99")
    @JsonProperty("price")
    private Double price;

    @Schema(description = "Compare at price (original price)", example = "2799.99")
    @JsonProperty("compareAtPrice")
    private BigDecimal compareAtPrice;

    @Schema(description = "Variant attributes (color, size, etc.)")
    @JsonProperty("attributes")
    private Map<String, String> attributes;

    @Schema(description = "Inventory information")
    @JsonProperty("inventory")
    private VariantInventoryResponse inventory;

    @Schema(description = "Variant-specific image URLs")
    @JsonProperty("imageUrls")
    private List<String> imageUrls;

    @Schema(description = "Variant weight", example = "2.1")
    @JsonProperty("weight")
    private Double weight;

    @Schema(description = "Variant dimensions")
    @JsonProperty("dimensions")
    private ProductResponse.ProductDimensionsResponse dimensions;

    @Schema(description = "Variant status", example = "ACTIVE")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Position for ordering variants", example = "1")
    @JsonProperty("position")
    private Integer position;

    @Schema(description = "Variant creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Variant last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Variant inventory response DTO
     */
    @Schema(description = "Variant inventory information")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInventoryResponse {
        @Schema(description = "Available quantity", example = "150")
        @JsonProperty("quantity")
        private Integer quantity;

        @Schema(description = "Reserved quantity", example = "5")
        @JsonProperty("reservedQuantity")
        private Integer reservedQuantity;

        @Schema(description = "Low stock threshold", example = "10")
        @JsonProperty("lowStockThreshold")
        private Integer lowStockThreshold;

        @Schema(description = "Whether inventory is tracked", example = "true")
        @JsonProperty("trackInventory")
        private boolean trackInventory;

        @Schema(description = "Whether backorder is allowed", example = "false")
        @JsonProperty("allowBackorder")
        private boolean allowBackorder;

        @Schema(description = "Inventory policy", example = "DENY")
        @JsonProperty("inventoryPolicy")
        private String inventoryPolicy;

        @Schema(description = "Available quantity for purchase", example = "145")
        @JsonProperty("availableQuantity")
        private Integer availableQuantity;

        @Schema(description = "Whether variant is in stock", example = "true")
        @JsonProperty("inStock")
        private boolean inStock;

        @Schema(description = "Whether variant is low on stock", example = "false")
        @JsonProperty("lowStock")
        private boolean lowStock;
    }
}
