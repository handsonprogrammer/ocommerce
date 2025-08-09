package com.ocommerce.services.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product variant response DTO
 */
@Schema(description = "Product variant information")
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

    @Schema(description = "Variant position for ordering", example = "1")
    @JsonProperty("position")
    private Integer position;

    @Schema(description = "Variant creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "Variant last update timestamp")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public ProductVariantResponse() {}

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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public VariantInventoryResponse getInventory() {
        return inventory;
    }

    public void setInventory(VariantInventoryResponse inventory) {
        this.inventory = inventory;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public ProductResponse.ProductDimensionsResponse getDimensions() {
        return dimensions;
    }

    public void setDimensions(ProductResponse.ProductDimensionsResponse dimensions) {
        this.dimensions = dimensions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
     * Variant inventory response DTO
     */
    public static class VariantInventoryResponse {
        @Schema(description = "Available quantity", example = "100")
        @JsonProperty("quantity")
        private Integer quantity;

        @Schema(description = "Reserved quantity", example = "5")
        @JsonProperty("reservedQuantity")
        private Integer reservedQuantity;

        @Schema(description = "Available quantity for sale", example = "95")
        @JsonProperty("availableQuantity")
        private Integer availableQuantity;

        @Schema(description = "Low stock threshold", example = "10")
        @JsonProperty("lowStockThreshold")
        private Integer lowStockThreshold;

        @Schema(description = "Whether inventory is tracked", example = "true")
        @JsonProperty("trackInventory")
        private boolean trackInventory;

        @Schema(description = "Whether backorders are allowed", example = "false")
        @JsonProperty("allowBackorder")
        private boolean allowBackorder;

        @Schema(description = "Inventory policy", example = "DENY")
        @JsonProperty("inventoryPolicy")
        private String inventoryPolicy;

        @Schema(description = "Whether variant is in stock", example = "true")
        @JsonProperty("inStock")
        private boolean inStock;

        @Schema(description = "Whether variant is low on stock", example = "false")
        @JsonProperty("lowStock")
        private boolean lowStock;

        // Constructors, getters, and setters
        public VariantInventoryResponse() {}

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
            return availableQuantity;
        }

        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
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

        public String getInventoryPolicy() {
            return inventoryPolicy;
        }

        public void setInventoryPolicy(String inventoryPolicy) {
            this.inventoryPolicy = inventoryPolicy;
        }

        public boolean isInStock() {
            return inStock;
        }

        public void setInStock(boolean inStock) {
            this.inStock = inStock;
        }

        public boolean isLowStock() {
            return lowStock;
        }

        public void setLowStock(boolean lowStock) {
            this.lowStock = lowStock;
        }
    }
}
