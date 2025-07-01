package com.ocommerce.api.mapper;

import com.ocommerce.api.constants.CartStatus;
import com.ocommerce.api.jpa.entities.Cart;
import com.ocommerce.api.jpa.entities.CartItem;
import com.ocommerce.api.model.CartDto;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {
    public static CartDto toDto(Cart cart) {

        if (cart == null)
            return null;
        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setStatus(cart.getStatus() != null ? cart.getStatus().name() : null);
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setShippingAddressId(cart.getShippingAddress() != null ? cart.getShippingAddress().getAddressId() : null);
        dto.setBillingAddressId(cart.getBillingAddress() != null ? cart.getBillingAddress().getAddressId() : null);
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalTax(cart.getTotalTax());
        dto.setTotalShippingCost(cart.getTotalShippingCost());
        dto.setLocked(cart.isLocked());
        dto.setCreateAt(cart.getCreatedAt());
        dto.setEditedAt(cart.getUpdatedAt());
        dto.setCartItems(cart.getCartItems().stream().map(CartMapper::toCartItemDto).collect(Collectors.toList()));
        return dto;
    }

    public static Cart toEntity(CartDto dto) {
        if (dto == null)
            return null;
        Cart cart = new Cart();
        cart.setCartId(dto.getCartId());
        if (dto.getStatus() != null) {
            cart.setStatus(CartStatus.valueOf(dto.getStatus()));
        }
        // User, addresses, and cartItems should be set in the service layer
        cart.setTotalAmount(dto.getTotalAmount());
        cart.setTotalTax(dto.getTotalTax());
        cart.setTotalShippingCost(dto.getTotalShippingCost());
        cart.setLocked(dto.isLocked());
        cart.setCreatedAt(dto.getCreateAt());
        cart.setUpdatedAt(dto.getEditedAt());
        return cart;
    }

    public static CartDto.CartItemDto toCartItemDto(CartItem item) {
        CartDto.CartItemDto dto = new CartDto.CartItemDto();
        dto.setCartItemId(item.getCartItems_Id());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : null);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setTax(item.getTax());
        dto.setShippingCost(item.getShippingCost());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setDiscount(item.getDiscount());
        dto.setUserId(item.getUser() != null ? item.getUser().getId() : null);
        dto.setCreateAt(item.getCreateAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static CartItem toCartItemEntity(CartDto.CartItemDto dto) {
        if (dto == null)
            return null;
        CartItem item = new CartItem();
        item.setCartItems_Id(dto.getCartItemId());
        // Product and user should be set in the service layer
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        item.setTotalPrice(dto.getTotalPrice());
        item.setTax(dto.getTax());
        item.setShippingCost(dto.getShippingCost());
        item.setTotalAmount(dto.getTotalAmount());
        item.setDiscount(dto.getDiscount());
        item.setCreateAt(dto.getCreateAt());
        item.setUpdatedAt(dto.getUpdatedAt());
        return item;
    }

    public static List<CartDto> toDtoList(List<Cart> carts) {
        return carts.stream()
                .map(CartMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<CartDto.CartItemDto> toCartItemDtoList(List<CartItem> items) {
        return items.stream()
                .map(CartMapper::toCartItemDto)
                .collect(Collectors.toList());
    }
}