package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class CartResponseDTO {
    private UUID id;
    private UUID userId;
    private List<CartItemResponseDTO> items;
    private UUID shippingAddressId;
    private UUID billingAddressId;
    private BigDecimal totalAmount;
}

