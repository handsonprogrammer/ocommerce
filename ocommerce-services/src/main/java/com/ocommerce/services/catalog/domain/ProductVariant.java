package com.ocommerce.services.catalog.domain;

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
public class ProductVariant {

    @Id
    @Field("variant_id")
    private UUID variantId;

    @Field("sku")
    @Indexed(unique = true)
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

    // Constructors
    public ProductVariant() {
        this.variantId = UUID.randomUUID();
        this.status = VariantStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public void setCompareAtPrice(BigDecimal compareAtPrice) {
        this.compareAtPrice = compareAtPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public VariantInventory getInventory() {
        return inventory;
    }

    public void setInventory(VariantInventory inventory) {
        this.inventory = inventory;
    }

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Product.ProductDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Product.ProductDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public VariantStatus getStatus() {
        return status;
    }

    public void setStatus(VariantStatus status) {
        this.status = status;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
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
     * Variant status enumeration
     */
    public enum VariantStatus {
        ACTIVE,
        INACTIVE,
        DISCONTINUED
    }

    /**
     * Embedded inventory information for variants
     */
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

        // Constructors
        public VariantInventory() {
            this.quantity = 0;
            this.reservedQuantity = 0;
            this.trackInventory = true;
            this.allowBackorder = false;
            this.inventoryPolicy = InventoryPolicy.DENY;
        }

        // Getters and Setters
        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getReservedQuantity() {
            return reservedQuantity;
        }

        public void setReservedQuantity(Integer reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }

        public Integer getAvailableQuantity() {
            return quantity - reservedQuantity;
        }

        public Integer getLowStockThreshold() {
            return lowStockThreshold;
        }

        public void setLowStockThreshold(Integer lowStockThreshold) {
            this.lowStockThreshold = lowStockThreshold;
        }

        public boolean isTrackInventory() {
            return trackInventory;
        }

        public void setTrackInventory(boolean trackInventory) {
            this.trackInventory = trackInventory;
        }

        public boolean isAllowBackorder() {
            return allowBackorder;
        }

        public void setAllowBackorder(boolean allowBackorder) {
            this.allowBackorder = allowBackorder;
        }

        public InventoryPolicy getInventoryPolicy() {
            return inventoryPolicy;
        }

        public void setInventoryPolicy(InventoryPolicy inventoryPolicy) {
            this.inventoryPolicy = inventoryPolicy;
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
