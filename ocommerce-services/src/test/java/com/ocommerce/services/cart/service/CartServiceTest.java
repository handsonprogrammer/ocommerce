package com.ocommerce.services.cart.service;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.domain.CartItem;
import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.repository.CartRepository;
import com.ocommerce.services.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductValidationService productValidationService;

    @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID productId;
    private UUID variantId;
    private Cart cart;
    private CartItem cartItem;
    private ProductPricingInfo pricingInfo;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        cartItem.setProductId(productId);
        cartItem.setVariantId(variantId);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(BigDecimal.valueOf(100));
        cartItem.setCart(cart);

        pricingInfo = ProductPricingInfo.builder()
            .productId(productId)
            .variantId(variantId)
            .productName("Test Product")
            .variantName("Test Variant")
            .sku("TEST-SKU")
            .price(BigDecimal.valueOf(100))
            .unitOfMeasure("piece")
            .inventoryTracking(true)
            .availableStock(10)
            .isActive(true)
            .build();
    }

    @Test
    void addItem_shouldCreateNewCartIfNotExists() {
        // Given
        when(productValidationService.validateAndGetProductPricing(productId, variantId)).thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(productId, variantId, 2)).thenReturn(true);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.addItem(userId, productId, variantId, 2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
        verify(productValidationService).validateAndGetProductPricing(productId, variantId);
        verify(productValidationService).validateStockAvailability(productId, variantId, 2);
    }

    @Test
    void addItem_shouldAddItemToExistingCart() {
        // Given
        when(productValidationService.validateAndGetProductPricing(productId, variantId)).thenReturn(pricingInfo);
        when(productValidationService.validateStockAvailability(productId, variantId, 2)).thenReturn(true);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.addItem(userId, productId, variantId, 2);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).save(cart);
        verify(productValidationService).validateAndGetProductPricing(productId, variantId);
        verify(productValidationService).validateStockAvailability(productId, variantId, 2);
    }

    @Test
    void removeItem_shouldRemoveItemFromCart() {
        // Given
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.removeItem(userId, cartItem.getId());

        // Then
        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void updateItemQuantity_shouldUpdateQuantity() {
        // Given
        cart.getItems().add(cartItem);
        when(productValidationService.validateStockAvailability(productId, variantId, 5)).thenReturn(true);
        when(productValidationService.validateAndGetProductPricing(productId, variantId)).thenReturn(pricingInfo);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.updateItemQuantity(userId, cartItem.getId(), 5);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).save(cart);
        verify(productValidationService).validateStockAvailability(productId, variantId, 5);
        verify(productValidationService).validateAndGetProductPricing(productId, variantId);
    }

    @Test
    void getCartByUserId_shouldReturnCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // When
        Optional<Cart> result = cartService.getCartByUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cart);
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void setShippingAddress_shouldSetAddress() {
        // Given
        UUID addressId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.setShippingAddress(userId, addressId);

        // Then
        assertThat(result.getShippingAddressId()).isEqualTo(addressId);
        verify(cartRepository).save(cart);
    }

    @Test
    void setBillingAddress_shouldSetAddress() {
        // Given
        UUID addressId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.setBillingAddress(userId, addressId);

        // Then
        assertThat(result.getBillingAddressId()).isEqualTo(addressId);
        verify(cartRepository).save(cart);
    }
}
