package com.ocommerce.api.jpa.repositories;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import com.ocommerce.api.jpa.entities.CartItem;

public interface CartItemsRepository
        extends ListCrudRepository<CartItem, Long>, ListPagingAndSortingRepository<CartItem, Long> {

    /** Find by cartId and UserId. */
    List<CartItem> findByCartIdAndUserId(Long cartId, Long userId);

    /** Find by cartItemId and UserId. */
    CartItem findByCartItemsIdAndUserId(Long cartItemsId, Long userId);

}
