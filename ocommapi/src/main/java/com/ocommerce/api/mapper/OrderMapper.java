package com.ocommerce.api.mapper;

import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.OrderItems;
import com.ocommerce.api.model.OrderDto;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderDto toDto(Order order) {
        if (order == null)
            return null;
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser().getId());
        dto.setShippingAddressId(order.getShippingAddress() != null ? order.getShippingAddress().getAddressId() : null);
        dto.setBillingAddressId(order.getBillingAddress() != null ? order.getBillingAddress().getAddressId() : null);
        dto.setItems(order.getOrderItems().stream().map(OrderMapper::toOrderItemDto).collect(Collectors.toList()));
        return dto;
    }

    public static OrderDto.OrderItemDto toOrderItemDto(OrderItems item) {
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setProductId(item.getProduct().getProductId());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}