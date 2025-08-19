package com.ocommerce.services.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartItemResponseDTO {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;

    // Additional validated product information from catalog domain
    private String productName;
    private String variantName;
    private String sku;
}
