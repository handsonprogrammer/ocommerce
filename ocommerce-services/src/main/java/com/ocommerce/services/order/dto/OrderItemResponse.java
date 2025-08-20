package com.ocommerce.services.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Order item response")
@Data
public class OrderItemResponse {
    @Schema(description = "Order item unique identifier", example = "550e8400-e29b-41d4-a716-446655440110")
    private UUID id;

    @Schema(description = "Product ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID productId;

    @Schema(description = "Variant ID of the product (if applicable)", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID variantId;

    @Schema(description = "Quantity of product", example = "1")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "99.99")
    private BigDecimal unitPrice;

    @Schema(description = "Discount amount applied", example = "10.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tax amount applied", example = "5.00")
    private BigDecimal taxAmount;

    @Schema(description = "Total price for this order item", example = "94.99")
    private BigDecimal totalPrice;
}

