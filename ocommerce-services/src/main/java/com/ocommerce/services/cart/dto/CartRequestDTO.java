package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CartRequestDTO {
    private UUID shippingAddressId;
    private UUID billingAddressId;
}

