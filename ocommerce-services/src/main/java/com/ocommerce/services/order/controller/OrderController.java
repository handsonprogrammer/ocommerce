package com.ocommerce.services.order.controller;

import com.ocommerce.services.order.dto.CreateOrderRequestDTO;
import com.ocommerce.services.order.dto.OrderResponseDTO;
import com.ocommerce.services.order.mapper.OrderMapper;
import com.ocommerce.services.order.service.OrderService;
import com.ocommerce.services.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        var order = orderService.createOrderFromCart(
            user.getId(),
            requestDTO.getShippingAddressId(),
            requestDTO.getBillingAddressId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderMapper.toOrderResponseDTO(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        return orderService.getOrderById(id, user.getId())
            .map(orderMapper::toOrderResponseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getUserOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> orders = orderService.getUserOrders(user.getId(), pageable)
            .map(orderMapper::toOrderResponseDTO);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        var cancelledOrder = orderService.cancelOrder(id, user.getId());
        return ResponseEntity.ok(orderMapper.toOrderResponseDTO(cancelledOrder));
    }
}
