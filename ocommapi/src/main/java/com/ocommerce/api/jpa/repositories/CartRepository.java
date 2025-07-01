package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Cart;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends ListCrudRepository<Cart, Long> {

    Optional<Cart> findByCartId(Long cartId);

    // Add more query methods as needed, e.g.:
    // List<Cart> findByUser(UserReg user);
    /**
     * Finds the list of active carts for a given user.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the first active cart for the user, or empty
     *         if none found.
     */
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.status = com.ocommerce.api.constants.CartStatus.ACTIVE")
    List<Cart> findActiveCartsByUserId(Long userId);

    /**
     * Updates the status of all carts belonging to a user to INACTIVE, except for
     * the cart with the specified cartId.
     *
     * @param userId The ID of the user whose carts are to be updated.
     * @param cartId The ID of the cart that should remain active.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Cart c SET c.status = com.ocommerce.api.constants.CartStatus.INACTIVE WHERE c.user.id = :userId AND c.cartId <> :cartId AND c.status <> com.ocommerce.api.constants.CartStatus.PLACED")
    void updateOtherPendingCartsToInactive(Long userId, Long cartId);

}