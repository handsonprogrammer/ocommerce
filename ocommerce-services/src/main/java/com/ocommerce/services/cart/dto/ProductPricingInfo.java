package com.ocommerce.services.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for product pricing information retrieved from catalog domain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product pricing information DTO")
public class ProductPricingInfo {
    @Schema(description = "Product unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID productId;

    @Schema(description = "Variant unique identifier", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID variantId;

    @Schema(description = "Product name", example = "MacBook Pro 16-inch")
    private String productName;

    @Schema(description = "Variant name", example = "Space Gray, 32GB RAM")
    private String variantName;

    @Schema(description = "SKU code", example = "MBP16-SG-32GB")
    private String sku;

    @Schema(description = "Current price", example = "2499.99")
    private BigDecimal price;

    @Schema(description = "Compare at price (original price before discount)", example = "2699.99")
    private BigDecimal compareAtPrice;

    @Schema(description = "Unit of measure", example = "piece")
    private String unitOfMeasure;

    @Schema(description = "Whether inventory tracking is enabled", example = "true")
    private boolean inventoryTracking;

    @Schema(description = "Available stock quantity", example = "15")
    private Integer availableStock;

    @Schema(description = "Whether the product is active", example = "true")
    private boolean isActive;
}
