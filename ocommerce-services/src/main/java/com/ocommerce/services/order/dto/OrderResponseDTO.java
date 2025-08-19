package com.ocommerce.services.order.dto;

import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.domain.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDTO {
    private UUID id;
    private UUID userId;
    private List<OrderItemResponseDTO> items;
    private UUID shippingAddressId;
    private UUID billingAddressId;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
