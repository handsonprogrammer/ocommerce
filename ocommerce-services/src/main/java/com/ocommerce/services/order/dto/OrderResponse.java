package com.ocommerce.services.order.dto;

import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order response")
@Data
public class OrderResponse {
    @Schema(description = "Order unique identifier", example = "550e8400-e29b-41d4-a716-446655440100")
    private UUID id;

    @Schema(description = "User ID who placed the order", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID userId;

    @Schema(description = "List of items in the order")
    private List<OrderItemResponse> items;

    @Schema(description = "Shipping address ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID shippingAddressId;

    @Schema(description = "Billing address ID", example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID billingAddressId;

    @Schema(description = "Order status", example = "PLACED")
    private OrderStatus orderStatus;

    @Schema(description = "Payment status", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Total amount for the order", example = "299.99")
    private BigDecimal totalAmount;

    @Schema(description = "Order creation timestamp (UTC)", example = "2024-08-19T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Order last update timestamp (UTC)", example = "2024-08-19T12:00:00Z")
    private Instant updatedAt;
}

