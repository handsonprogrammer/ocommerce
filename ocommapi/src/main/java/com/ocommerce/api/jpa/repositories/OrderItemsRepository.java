package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.OrderItems;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface OrderItemsRepository extends ListCrudRepository<OrderItems, Long> {
}