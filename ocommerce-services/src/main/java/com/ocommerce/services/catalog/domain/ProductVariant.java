package com.ocommerce.services.catalog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Product variant embedded document within Product collection
 */
@Data
@NoArgsConstructor
public class ProductVariant {

    @Id
    @Field("variant_id")
    private UUID variantId;

    @Field("sku")
    @Indexed(unique = true, partialFilter = "{'sku': { $exists: true } }")
    private String sku;

    @Field("barcode")
    private String barcode;

    @Field("variant_name")
    private String variantName;

    @Field("price")
    private Double price;

    @Field("compare_at_price")
    private BigDecimal compareAtPrice;

    @Field("cost_price")
    private BigDecimal costPrice;

    @Field("attributes")
    private Map<String, String> attributes; // e.g., {"color": "Red", "size": "Large"}

    @Field("inventory")
    private VariantInventory inventory;

    @Field("images")
    private java.util.List<String> imageUrls;

    @Field("weight")
    private Double weight;

    @Field("dimensions")
    private Product.ProductDimensions dimensions;

    @Field("status")
    private VariantStatus status;

    @Field("position")
    private Integer position; // For ordering variants

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Custom initialization
    public void initializeDefaults() {
        if (this.variantId == null) {
            this.variantId = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = VariantStatus.ACTIVE;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Variant status enumeration
     */
    public enum VariantStatus {
        ACTIVE,
        INACTIVE,
        DISCONTINUED
    }

    /**
     * Variant inventory tracking
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInventory {
        @Field("quantity")
        private Integer quantity;

        @Field("reserved_quantity")
        private Integer reservedQuantity;

        @Field("low_stock_threshold")
        private Integer lowStockThreshold;

        @Field("track_inventory")
        private boolean trackInventory;

        @Field("allow_backorder")
        private boolean allowBackorder;

        @Field("inventory_policy")
        private InventoryPolicy inventoryPolicy;

        // Helper methods
        public Integer getAvailableQuantity() {
            return quantity - reservedQuantity;
        }

        public boolean isInStock() {
            return !trackInventory || getAvailableQuantity() > 0;
        }

        public boolean isLowStock() {
            return trackInventory && lowStockThreshold != null &&
                   getAvailableQuantity() <= lowStockThreshold;
        }

        /**
         * Inventory policy for out-of-stock scenarios
         */
        public enum InventoryPolicy {
            DENY,    // Don't sell when out of stock
            CONTINUE // Allow selling when out of stock
        }
    }
}
