package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Cart request")
public class CartRequest {
    @Schema(description = "Shipping address ID for the cart", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID shippingAddressId;

    @Schema(description = "Billing address ID for the cart", example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID billingAddressId;
}

