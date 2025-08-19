package com.ocommerce.services.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemResponseDTO {
    private UUID id;
    private UUID productId;
    private UUID variantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
}
