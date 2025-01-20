package com.ocommerce.api.service;

import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.OrderRepository;
import com.ocommerce.api.jpa.repositories.UserRegRepository;
import com.ocommerce.api.model.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private UserRegRepository userRegRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Gets the list of orders for a given user.
     * @param userId The user to search for.
     * @return The list of orders.
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUser_Id(userId);
    }
}
