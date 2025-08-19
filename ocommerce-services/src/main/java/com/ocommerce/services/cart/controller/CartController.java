package com.ocommerce.services.cart.controller;

import com.ocommerce.services.cart.dto.*;
import com.ocommerce.services.cart.mapper.CartMapper;
import com.ocommerce.services.cart.service.CartService;
import com.ocommerce.services.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for shopping cart management endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Shopping Cart", description = "Shopping cart operations including adding items, managing quantities, and checkout")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {
    private final CartService cartService;
    private final CartMapper cartMapper;

    /**
     * Get current user's shopping cart
     */
    @GetMapping
    @Operation(summary = "Get user's cart", description = "Retrieve the current authenticated user's shopping cart with all items and calculated totals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart not found for user")
    })
    public ResponseEntity<CartResponseDTO> getCart(@AuthenticationPrincipal User user) {
        log.info("Get cart request for user: {}", user.getId());
        return cartService.getCartByUserId(user.getId())
                .map(cartMapper::toCartResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add item to shopping cart
     */
    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product or variant to the user's shopping cart with validated pricing from catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product or variant not found")
    })
    public ResponseEntity<CartResponseDTO> addItem(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Cart item details including product ID, variant ID (optional), and quantity", required = true)
            @Valid @RequestBody CartItemRequestDTO itemDto) {

        log.info("Add item to cart request for user: {}, product: {}, quantity: {}",
                user.getId(), itemDto.getProductId(), itemDto.getQuantity());

        var cart = cartService.addItem(
            user.getId(),
            itemDto.getProductId(),
            itemDto.getVariantId(),
            itemDto.getQuantity()
        );
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Update quantity of cart item
     */
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific item in the user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Cart item ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID itemId,
            @Parameter(description = "New quantity for the item", required = true, example = "3")
            @RequestParam int quantity) {

        log.info("Update cart item quantity request for user: {}, item: {}, quantity: {}",
                user.getId(), itemId, quantity);

        var cart = cartService.updateItemQuantity(user.getId(), itemId, quantity);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Remove item from shopping cart
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the user's shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed from cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponseDTO> removeItem(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Cart item ID to remove", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID itemId) {

        log.info("Remove cart item request for user: {}, item: {}", user.getId(), itemId);

        var cart = cartService.removeItem(user.getId(), itemId);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Set shipping address for cart
     */
    @PutMapping("/shipping-address")
    @Operation(summary = "Set shipping address", description = "Set or update the shipping address for the user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipping address set successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid address data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<CartResponseDTO> setShippingAddress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Shipping address details", required = true)
            @Valid @RequestBody CartRequestDTO dto) {

        log.info("Set shipping address request for user: {}, address: {}",
                user.getId(), dto.getShippingAddressId());

        var cart = cartService.setShippingAddress(user.getId(), dto.getShippingAddressId());
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Set billing address for cart
     */
    @PutMapping("/billing-address")
    @Operation(summary = "Set billing address", description = "Set or update the billing address for the user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Billing address set successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid address data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<CartResponseDTO> setBillingAddress(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Billing address details", required = true)
            @Valid @RequestBody CartRequestDTO dto) {

        log.info("Set billing address request for user: {}, address: {}",
                user.getId(), dto.getBillingAddressId());

        var cart = cartService.setBillingAddress(user.getId(), dto.getBillingAddressId());
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Copy items from previous order to cart
     */
    @PostMapping("/copy-from-order/{orderId}")
    @Operation(summary = "Copy order items to cart", description = "Copy all items from a previous order into the current user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order items copied to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Some items may no longer be available"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<CartResponseDTO> copyItemsFromOrder(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Order ID to copy items from", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID orderId) {

        log.info("Copy items from order request for user: {}, order: {}", user.getId(), orderId);

        var cart = cartService.copyItemsFromOrder(user.getId(), orderId);
        return ResponseEntity.ok(cartMapper.toCartResponseDTO(cart));
    }

    /**
     * Merge guest cart with user cart
     */
    @PostMapping("/merge-guest-cart")
    @Operation(summary = "Merge guest cart", description = "Merge items from a guest cart into the authenticated user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guest cart merged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid guest cart ID or merge conflict"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Guest cart not found")
    })
    public ResponseEntity<Void> mergeGuestCart(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Guest cart ID to merge", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID guestCartId) {

        log.info("Merge guest cart request for user: {}, guest cart: {}", user.getId(), guestCartId);

        cartService.mergeGuestCart(guestCartId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Convert cart to order (checkout)
     */
    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Convert the user's cart to an order. Requires shipping and billing addresses to be set.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart converted to order successfully"),
            @ApiResponse(responseCode = "400", description = "Cart validation failed or missing required addresses"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart not found or empty"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock for one or more items")
    })
    public ResponseEntity<Void> convertCartToOrder(@AuthenticationPrincipal User user) {
        log.info("Convert cart to order request for user: {}", user.getId());

        cartService.convertCartToOrder(user.getId());
        return ResponseEntity.ok().build();
    }
}
