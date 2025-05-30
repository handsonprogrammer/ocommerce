package com.ocommerce.api.controller;

import com.ocommerce.api.exception.AddressNotFoundException;
import com.ocommerce.api.exception.ProductNotFoundException;
import com.ocommerce.api.exception.UserNotFoundException;
import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.model.AddOrderItemsRequest;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    /** The Order Service. */
    private OrderService orderService;

    /**
     * Constructor for spring injection.
     * 
     * @param orderService
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint to get all orders for a specific user.
     * 
     * @param user The user provided by spring security context.
     * @return The list of orders the user had made.
     */
    @GetMapping
    public List<Order> getOrders(@AuthenticationPrincipal UserDetails user) {
        return orderService.getOrdersByUserId(user.getUserId());
    }

    @PostMapping("/add-items")
    public ResponseEntity<Order> addItemsToOrder(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AddOrderItemsRequest request) {
        try {
            Order order = orderService.addItemsToOrder(user, request);
            return ResponseEntity.ok(order);
        } catch (ProductNotFoundException | UserNotFoundException | AddressNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
