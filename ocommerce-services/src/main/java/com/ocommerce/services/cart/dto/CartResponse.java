package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Cart response")
public class CartResponse {
    @Schema(description = "Cart unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User ID associated with the cart", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID userId;

    @Schema(description = "List of items in the cart")
    private List<CartItemResponse> items;

    @Schema(description = "Shipping address ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID shippingAddressId;

    @Schema(description = "Billing address ID", example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID billingAddressId;

    @Schema(description = "Total amount for the cart", example = "199.99")
    private BigDecimal totalAmount;
}

