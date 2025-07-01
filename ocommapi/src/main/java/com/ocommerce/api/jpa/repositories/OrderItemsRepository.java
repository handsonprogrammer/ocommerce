package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.OrderItems;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface OrderItemsRepository
        extends ListCrudRepository<OrderItems, Long>, ListPagingAndSortingRepository<OrderItems, Long> {

    List<OrderItems> findByOrder(Order order);

    /**
     * Find all order items by order ID.
     *
     * @param orderId the ID of the order
     * @return a list of order items associated with the specified order ID
     */
    List<OrderItems> findByOrderOrderId(Long orderId);
}