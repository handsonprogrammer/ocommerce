package com.ocommerce.services.order.mapper;

import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import com.ocommerce.services.order.dto.OrderResponse;
import com.ocommerce.services.order.dto.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "totalPrice", expression = "java(orderItem.getTotalPrice())")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    OrderResponse toOrderResponse(Order order);
}
