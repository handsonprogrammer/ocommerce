package com.ocommerce.api.controller;

import com.ocommerce.api.jpa.entities.Cart;
import com.ocommerce.api.jpa.entities.CartItem;
import com.ocommerce.api.model.AddToCartRequest;
import com.ocommerce.api.model.CartDto;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.model.CartDto.CartItemDto;
import com.ocommerce.api.mapper.CartMapper;
import com.ocommerce.api.service.CartService;
import com.ocommerce.api.exception.ProductNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add-item")
    public ResponseEntity<CartDto> addItemToCart(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AddToCartRequest request) {
        try {
            CartDto cart = cartService.addItemsToCart(user.getUserId(), request);
            return ResponseEntity.ok(cart);
        } catch (ProductNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update-item")
    public ResponseEntity<CartItemDto> updateCartItem(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam Long cartItemId,
            @RequestParam int quantity) {
        try {
            CartItem cartItem = cartService.updateCartItem(cartItemId, quantity, user.getUserId());
            return ResponseEntity.ok(CartMapper.toCartItemDto(cartItem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}