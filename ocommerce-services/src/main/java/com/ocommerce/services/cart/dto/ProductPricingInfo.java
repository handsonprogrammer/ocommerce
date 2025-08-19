package com.ocommerce.services.cart.dto;

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
public class ProductPricingInfo {
    private UUID productId;
    private UUID variantId;
    private String productName;
    private String variantName;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String unitOfMeasure;
    private boolean inventoryTracking;
    private Integer availableStock;
    private boolean isActive;
}
