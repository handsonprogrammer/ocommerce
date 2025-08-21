package com.ocommerce.services.cart.repository;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.domain.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private UUID userId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setShippingAddressId(UUID.randomUUID());
        cart.setBillingAddressId(UUID.randomUUID());

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        cart.getItems().add(item);
    }

    @Test
    void findByUserId_shouldReturnCartWhenExists() {
        // Given
        Cart savedCart = cartRepository.save(cart);

        // When
        Optional<Cart> result = cartRepository.findByUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCart.getId());
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getItems()).hasSize(1);
    }

    @Test
    void findByUserId_shouldReturnEmptyWhenNotExists() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistCartWithItems() {
        // When
        Cart savedCart = cartRepository.save(cart);

        // Then
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getUserId()).isEqualTo(userId);
        assertThat(savedCart.getItems()).hasSize(1);
        assertThat(savedCart.getItems().get(0).getId()).isNotNull();
        assertThat(savedCart.getCreatedAt()).isNotNull();
        assertThat(savedCart.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_shouldUpdateExistingCart() {
        // Given
        Cart savedCart = cartRepository.save(cart);
        UUID originalCartId = savedCart.getId();

        // Add another item
        CartItem newItem = new CartItem();
        newItem.setCart(savedCart);
        newItem.setProductId(UUID.randomUUID());
        newItem.setQuantity(1);
        newItem.setUnitPrice(BigDecimal.valueOf(50));
        savedCart.getItems().add(newItem);

        // When
        Cart updatedCart = cartRepository.save(savedCart);

        // Then
        assertThat(updatedCart.getId()).isEqualTo(originalCartId);
        assertThat(updatedCart.getItems()).hasSize(2);
    }

    @Test
    void delete_shouldRemoveCartAndItems() {
        // Given
        Cart savedCart = cartRepository.save(cart);
        UUID cartId = savedCart.getId();

        // When
        cartRepository.delete(savedCart);

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    @Test
    void getTotalAmount_shouldCalculateCorrectly() {
        // Given
        Cart savedCart = cartRepository.save(cart);

        // When
        BigDecimal totalAmount = savedCart.getTotalAmount();

        // Then
        assertThat(totalAmount).isEqualTo(BigDecimal.valueOf(200)); // 2 * 100
    }
}
