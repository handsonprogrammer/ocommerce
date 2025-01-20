package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface OrderRepository extends ListCrudRepository<Order, Long> {

    @Query("select o from Order o where o.user.id = ?1")
    List<Order> findByUser_Id(Long userId);
}