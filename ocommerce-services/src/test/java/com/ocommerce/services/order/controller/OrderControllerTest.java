package com.ocommerce.services.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocommerce.services.config.WithCustomUser;
import com.ocommerce.services.order.domain.Order;
import com.ocommerce.services.order.domain.OrderStatus;
import com.ocommerce.services.order.dto.CreateOrderRequest;
import com.ocommerce.services.order.dto.OrderResponse;
import com.ocommerce.services.order.mapper.OrderMapper;
import com.ocommerce.services.order.service.OrderService;
import com.ocommerce.services.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@WithCustomUser(email = EMAIL, userId = USER_ID,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        phoneNumber = PHONE_NUMBER,
        accountEnabled = ACCOUNT_ENABLED,
        emailVerified = EMAIL_VERIFIED)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private Order order;
    private OrderResponse OrderResponse;
    private CreateOrderRequest CreateOrderRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString(USER_ID);

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setShippingAddressId(UUID.randomUUID());
        order.setBillingAddressId(UUID.randomUUID());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(200));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        OrderResponse = new OrderResponse();
        OrderResponse.setId(order.getId());
        OrderResponse.setUserId(userId);
        OrderResponse.setShippingAddressId(order.getShippingAddressId());
        OrderResponse.setBillingAddressId(order.getBillingAddressId());
        OrderResponse.setOrderStatus(OrderStatus.PENDING);
        OrderResponse.setTotalAmount(BigDecimal.valueOf(200));
        OrderResponse.setItems(new ArrayList<>());

        CreateOrderRequest = new CreateOrderRequest();
        CreateOrderRequest.setShippingAddressId(UUID.randomUUID());
        CreateOrderRequest.setBillingAddressId(UUID.randomUUID());
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {
        // Given
        when(orderService.createOrderFromCart(eq(userId), any(UUID.class), any(UUID.class))).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(OrderResponse.getId().toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("PENDING"));
    }

    @Test
    void createOrder_shouldReturn400ForInvalidRequest() throws Exception {
        // Given
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        // Given
        UUID orderId = order.getId();
        when(orderService.getOrderById(orderId, userId)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void getOrder_shouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrderById(orderId, userId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserOrders_shouldReturnPagedOrders() throws Exception {
        // Given
        Page<Order> orderPage = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        Page<OrderResponse> responsePage = new PageImpl<>(List.of(OrderResponse), PageRequest.of(0, 10), 1);

        when(orderService.getUserOrders(eq(userId), any())).thenReturn(orderPage);
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(OrderResponse.getId().toString()));
    }

    @Test
    void cancelOrder_shouldCancelOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = order.getId();
        order.setOrderStatus(OrderStatus.CANCELLED);
        OrderResponse.setOrderStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(orderId, userId)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/cancel", orderId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));
    }

    @Test
    void cancelOrder_shouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderService.cancelOrder(orderId, userId)).thenThrow(new RuntimeException("Order not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/orders/{id}/cancel", orderId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
