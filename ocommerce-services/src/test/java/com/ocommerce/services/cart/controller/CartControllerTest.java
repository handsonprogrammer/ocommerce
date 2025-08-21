package com.ocommerce.services.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.dto.CartItemRequest;
import com.ocommerce.services.cart.dto.CartItemRequest;
import com.ocommerce.services.cart.dto.CartRequest;
import com.ocommerce.services.cart.dto.CartResponse;
import com.ocommerce.services.cart.mapper.CartMapper;
import com.ocommerce.services.cart.service.CartService;
import com.ocommerce.services.config.WithCustomUser;
import com.ocommerce.services.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.ocommerce.services.user.UserConstants.*;
import static com.ocommerce.services.user.UserConstants.ACCOUNT_ENABLED;
import static com.ocommerce.services.user.UserConstants.EMAIL_VERIFIED;
import static com.ocommerce.services.user.UserConstants.LAST_NAME;
import static com.ocommerce.services.user.UserConstants.PHONE_NUMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ActiveProfiles("test")
@WithCustomUser(email = EMAIL, userId = USER_ID,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        phoneNumber = PHONE_NUMBER,
        accountEnabled = ACCOUNT_ENABLED,
        emailVerified = EMAIL_VERIFIED)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartMapper cartMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private Cart cart;
    private CartResponse CartResponse;
    private CartItemRequest cartItemRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString(USER_ID);

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        CartResponse = new CartResponse();
        CartResponse.setId(cart.getId());
        CartResponse.setUserId(userId);
        CartResponse.setTotalAmount(BigDecimal.ZERO);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(UUID.randomUUID());
        cartItemRequest.setQuantity(2);
    }

    @Test
    void getCart_shouldReturnCart() throws Exception {
        // Given
        when(cartService.getCartByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void getCart_shouldReturn404WhenCartNotFound() throws Exception {
        // Given
        when(cartService.getCartByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_shouldAddItemToCart() throws Exception {
        // Given
        when(cartService.addItem(eq(userId), eq(cartItemRequest.getProductId()),
                                eq(cartItemRequest.getVariantId()), eq(cartItemRequest.getQuantity()))).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void addItem_shouldReturn400ForInvalidRequest() throws Exception {
        // Given
        CartItemRequest invalidRequest = new CartItemRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemQuantity_shouldUpdateQuantity() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(cartService.updateItemQuantity(userId, itemId, 5)).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                        .with(csrf())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void removeItem_shouldRemoveItem() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        when(cartService.removeItem(userId, itemId)).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void setShippingAddress_shouldSetAddress() throws Exception {
        // Given
        CartRequest requestDTO = new CartRequest();
        requestDTO.setShippingAddressId(UUID.randomUUID());
        when(cartService.setShippingAddress(userId, requestDTO.getShippingAddressId())).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/cart/shipping-address")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void setBillingAddress_shouldSetAddress() throws Exception {
        // Given
        CartRequest requestDTO = new CartRequest();
        requestDTO.setBillingAddressId(UUID.randomUUID());
        when(cartService.setBillingAddress(userId, requestDTO.getBillingAddressId())).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/cart/billing-address")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void copyItemsFromOrder_shouldCopyItems() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(cartService.copyItemsFromOrder(userId, orderId)).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(CartResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/cart/copy-from-order/{orderId}", orderId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(CartResponse.getId().toString()));
    }

    @Test
    void convertCartToOrder_shouldConvertSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/cart/checkout")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
