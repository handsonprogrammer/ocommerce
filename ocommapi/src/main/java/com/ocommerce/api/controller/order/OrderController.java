package com.ocommerce.api.controller.order;

import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.repositories.OrderRepository;
import com.ocommerce.api.model.UserDetails;
import com.ocommerce.api.service.OrderService;
import com.ocommerce.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
     * @param orderService
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint to get all orders for a specific user.
     * @param user The user provided by spring security context.
     * @return The list of orders the user had made.
     */
    @GetMapping
    public List<Order> getOrders(@AuthenticationPrincipal UserDetails user) {
        return orderService.getOrdersByUserId(user.getUserId());
    }
}
