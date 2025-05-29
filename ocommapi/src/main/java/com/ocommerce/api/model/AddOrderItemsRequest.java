package com.ocommerce.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddOrderItemsRequest {
    private Long orderId; // Optional: if null, create new order
    private List<OrderItemDto> items;
    private Long shippingAddressId;
    private Long billingAddressId;

    @Getter
    @Setter
    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
    }
}