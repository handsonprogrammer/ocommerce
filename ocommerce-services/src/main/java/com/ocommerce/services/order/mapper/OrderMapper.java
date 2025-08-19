package com.ocommerce.services.order.mapper;

import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderItem;
import com.ocommerce.services.order.dto.OrderResponseDTO;
import com.ocommerce.services.order.dto.OrderItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "totalPrice", expression = "java(orderItem.getTotalPrice())")
    OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem);

    List<OrderItemResponseDTO> toOrderItemResponseDTOList(List<OrderItem> items);

    OrderResponseDTO toOrderResponseDTO(Order order);
}
