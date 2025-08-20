package com.ocommerce.services.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Schema(description = "Cart item request")
@Data
public class CartItemRequest {
    @Schema(description = "Product ID to add to cart", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Schema(description = "Variant ID of the product (if applicable)", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID variantId;

    @Schema(description = "Quantity of product to add", example = "2")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}

