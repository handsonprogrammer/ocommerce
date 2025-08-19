package com.ocommerce.services.cart.mapper;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.domain.CartItem;
import com.ocommerce.services.cart.dto.CartResponseDTO;
import com.ocommerce.services.cart.dto.CartItemResponseDTO;
import com.ocommerce.services.cart.dto.CartItemRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(target = "totalAmount", expression = "java(cart.getTotalAmount())")
    CartResponseDTO toCartResponseDTO(Cart cart);

    List<CartItemResponseDTO> toCartItemResponseDTOList(List<CartItem> items);

    CartItemResponseDTO toCartItemResponseDTO(CartItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "variantName", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    CartItem toCartItem(CartItemRequestDTO dto);
}
