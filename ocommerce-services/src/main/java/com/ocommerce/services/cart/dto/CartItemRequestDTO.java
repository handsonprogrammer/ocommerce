package com.ocommerce.services.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequestDTO {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID variantId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
