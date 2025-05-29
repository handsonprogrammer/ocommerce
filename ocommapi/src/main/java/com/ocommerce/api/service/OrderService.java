package com.ocommerce.api.service;

import com.ocommerce.api.exception.AddressNotFoundException;
import com.ocommerce.api.exception.ProductNotFoundException;
import com.ocommerce.api.exception.UserNotFoundException;
import com.ocommerce.api.jpa.entities.Address;
import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.OrderItems;
import com.ocommerce.api.jpa.entities.Product;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.OrderRepository;
import com.ocommerce.api.model.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.ocommerce.api.model.AddOrderItemsRequest;

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
    public Order addItemsToOrder(UserDetails user, AddOrderItemsRequest request)
            throws ProductNotFoundException, UserNotFoundException, AddressNotFoundException {
        Order order;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            if (!order.getUser().getId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Order does not belong to user");
            }
        } else {
            UserReg userReg = userService.getUserById(user.getUserId());
            if (userReg == null) {
                throw new IllegalArgumentException("User not found");
            }
            order = new Order();
            order.setUser(userReg);
            // You may want to set addresses here as well
            order = orderRepository.save(order);

            // Assign addresses
            if (request.getShippingAddressId() != null) {
                Address shipping = addressService.getAddressById(request.getShippingAddressId());
                order.setShippingAddress(shipping);
            }
            if (request.getBillingAddressId() != null) {
                Address billing = addressService.getAddressById(request.getBillingAddressId());
                order.setBillingAddress(billing);
            }
            order = orderRepository.save(order);
        }

        for (AddOrderItemsRequest.OrderItemDto itemDto : request.getItems()) {
            Product product = productService.getProductById(itemDto.getProductId());
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            order.getQuantities().add(orderItem);
        }
        return orderRepository.save(order);
    }
}
