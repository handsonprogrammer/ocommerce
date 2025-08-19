package com.ocommerce.services.cart.controller;

import com.ocommerce.services.cart.dto.*;
import com.ocommerce.services.cart.mapper.CartMapper;
import com.ocommerce.services.cart.service.CartService;
import com.ocommerce.services.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {
    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(@AuthenticationPrincipal User user) {
        return cartService.getCartByUserId(user.getId())
                .map(cartMapper::toCartResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequestDTO itemDto) {
        // Use the new CartService method signature with validated product pricing
        var cart = cartService.addItem(
            user.getId(),
            itemDto.getProductId(),
            itemDto.getVariantId(),
            itemDto.getQuantity()
        );
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable UUID itemId,
            @RequestParam int quantity) {
        var cart = cartService.updateItemQuantity(user.getId(), itemId, quantity);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID itemId) {
        var cart = cartService.removeItem(user.getId(), itemId);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @PutMapping("/shipping-address")
    public ResponseEntity<CartResponseDTO> setShippingAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartRequestDTO dto) {
        var cart = cartService.setShippingAddress(user.getId(), dto.getShippingAddressId());
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @PutMapping("/billing-address")
    public ResponseEntity<CartResponseDTO> setBillingAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartRequestDTO dto) {
        var cart = cartService.setBillingAddress(user.getId(), dto.getBillingAddressId());
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @PostMapping("/copy-from-order/{orderId}")
    public ResponseEntity<CartResponseDTO> copyItemsFromOrder(
            @AuthenticationPrincipal User user,
            @PathVariable UUID orderId) {
        var cart = cartService.copyItemsFromOrder(user.getId(), orderId);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    @PostMapping("/merge-guest-cart")
    public ResponseEntity<Void> mergeGuestCart(
            @AuthenticationPrincipal User user,
            @RequestParam UUID guestCartId) {
        cartService.mergeGuestCart(guestCartId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Void> convertCartToOrder(@AuthenticationPrincipal User user) {
        cartService.convertCartToOrder(user.getId());
        return ResponseEntity.ok().build();
    }
}
