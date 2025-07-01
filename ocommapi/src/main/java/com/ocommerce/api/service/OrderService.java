package com.ocommerce.api.service;

import com.ocommerce.api.exception.AddressNotFoundException;
import com.ocommerce.api.exception.ProductNotFoundException;
import com.ocommerce.api.exception.UserNotFoundException;
import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.OrderItems;
import com.ocommerce.api.jpa.entities.Product;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.OrderRepository;
import com.ocommerce.api.mapper.AddressMapper;
import com.ocommerce.api.model.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.ocommerce.api.model.AddOrderItemsRequest;
import com.ocommerce.api.model.AddressDto;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private UserService userService;
    private ProductService productService;
    private AddressService addressService;

    public OrderService(OrderRepository orderRepository, UserService userService,
            ProductService productService, AddressService addressService) {
        this.userService = userService;
        this.productService = productService;
        this.addressService = addressService;
        this.orderRepository = orderRepository;
    }

    /**
     * Gets the list of orders for a given user.
     * 
     * @param userId The user to search for.
     * @return The list of orders.
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUser_Id(userId);
    }

    @Transactional
    public Order addItemToOrder(UserDetails user, AddOrderItemsRequest request)
            throws ProductNotFoundException, UserNotFoundException, AddressNotFoundException {

        UserReg userReg = userService.getUserById(user.getUserId());
        if (userReg == null) {
            throw new IllegalArgumentException("User not found");
        }
        Order order = getPendingOrderByUserId(userReg);

        Product product = productService.getProductById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + request.getProductId());
        }
        OrderItems orderItem = new OrderItems();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.getQuantity());
        order.getOrderItems().add(orderItem);
        return orderRepository.save(order);
    }

    // Get Pending Order
    public Order getPendingOrderByUserId(UserReg userReg) {
        return orderRepository.findFirstPendingOrderByUserIdAndNotLocked(userReg.getId())
                .orElseGet(() -> {
                    Order order = new Order();
                    order.setUser(userReg);
                    return orderRepository.save(order);
                });
    }
}
