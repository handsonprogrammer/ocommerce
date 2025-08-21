package com.ocommerce.services.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
@Schema(description = "Create order request")
public class CreateOrderRequest {
    @Schema(description = "Shipping address ID", example = "550e8400-e29b-41d4-a716-446655440002")
    @NotNull(message = "Shipping address ID is required")
    private UUID shippingAddressId;

    @Schema(description = "Billing address ID", example = "550e8400-e29b-41d4-a716-446655440003")
    @NotNull(message = "Billing address ID is required")
    private UUID billingAddressId;
}
