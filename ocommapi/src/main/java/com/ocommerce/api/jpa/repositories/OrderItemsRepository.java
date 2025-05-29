package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Order;
import com.ocommerce.api.jpa.entities.OrderItems;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface OrderItemsRepository
        extends ListCrudRepository<OrderItems, Long>, ListPagingAndSortingRepository<OrderItems, Long> {

    List<OrderItems> findByOrderId(Long orderId);

    List<OrderItems> findByOrder(Order order);
}