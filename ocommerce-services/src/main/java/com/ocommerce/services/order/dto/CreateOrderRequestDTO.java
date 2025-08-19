package com.ocommerce.services.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequestDTO {
    @NotNull(message = "Shipping address ID is required")
    private UUID shippingAddressId;

    @NotNull(message = "Billing address ID is required")
    private UUID billingAddressId;
}
