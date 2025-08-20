package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Cart item response")
public class CartItemResponse {
    @Schema(description = "Cart item unique identifier", example = "550e8400-e29b-41d4-a716-446655440010")
    private UUID id;

    @Schema(description = "Product ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID productId;

    @Schema(description = "Variant ID of the product (if applicable)", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID variantId;

    @Schema(description = "Quantity of product", example = "2")
    private int quantity;

    @Schema(description = "Unit price of the product", example = "99.99")
    private BigDecimal unitPrice;

    @Schema(description = "Discount amount applied", example = "10.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tax amount applied", example = "5.00")
    private BigDecimal taxAmount;

    @Schema(description = "Total price for this cart item", example = "189.98")
    private BigDecimal totalPrice;

    @Schema(description = "Product name", example = "MacBook Pro 16-inch")
    private String productName;

    @Schema(description = "Variant name", example = "Space Gray, 32GB RAM")
    private String variantName;

    @Schema(description = "SKU code", example = "MBP16-SG-32GB")
    private String sku;
}

