package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends ListCrudRepository<Order, Long> {

    @Query("select o from Order o where o.user.id = ?1")
    List<Order> findByUser_Id(Long userId);

    /**
     * This query retrieves the first pending order for a user that is not locked,
     * ordered by the most recently updated.
     */
    @Query("select o from Order o where o.user.id = ?1 and o.status = com.ocommerce.api.constants.OrderStatus.PENDING and o.isLocked = false order by o.updatedAt desc")
    Optional<Order> findFirstPendingOrderByUserIdAndNotLocked(Long userId);
}