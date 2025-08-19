package com.ocommerce.services.cart.service;

import com.ocommerce.services.cart.domain.Cart;
import com.ocommerce.services.cart.domain.CartItem;
import com.ocommerce.services.cart.dto.ProductPricingInfo;
import com.ocommerce.services.cart.exception.ProductValidationException;
import com.ocommerce.services.cart.repository.CartRepository;
import com.ocommerce.services.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final OrderService orderService;
    private final ProductValidationService productValidationService;

    @Transactional
    public Cart addItem(UUID userId, UUID productId, UUID variantId, int quantity) {
        log.info("Adding item to cart for user {}: product={}, variant={}, quantity={}",
                 userId, productId, variantId, quantity);

        // Validate product and get pricing information
        ProductPricingInfo pricingInfo = productValidationService.validateAndGetProductPricing(productId, variantId);

        // Validate stock availability
        if (!productValidationService.validateStockAvailability(productId, variantId, quantity)) {
            throw new ProductValidationException("Insufficient stock for product: " + productId);
        }

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return newCart;
        });

        if (cart.getItems() == null) {
            cart.setItems(new java.util.ArrayList<>());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId) &&
                           (variantId == null ? item.getVariantId() == null : variantId.equals(item.getVariantId())))
            .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Re-validate stock for new quantity
            if (!productValidationService.validateStockAvailability(productId, variantId, newQuantity)) {
                throw new ProductValidationException("Insufficient stock for requested quantity: " + newQuantity);
            }

            item.setQuantity(newQuantity);
            // Update pricing with current prices
            updateCartItemPricing(item, pricingInfo);
        } else {
            // Create new cart item
            CartItem newItem = createCartItem(cart, pricingInfo, quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(UUID userId, UUID itemId) {
        log.info("Removing item {} from cart for user {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (cart.getItems() != null) {
            cart.getItems().removeIf(i -> i.getId().equals(itemId));
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(UUID userId, UUID itemId, int quantity) {
        log.info("Updating item {} quantity to {} for user {}", itemId, quantity, userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (cart.getItems() != null) {
            CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));

            // Validate stock for new quantity
            if (!productValidationService.validateStockAvailability(item.getProductId(), item.getVariantId(), quantity)) {
                throw new ProductValidationException("Insufficient stock for requested quantity: " + quantity);
            }

            // Get current pricing and update item
            ProductPricingInfo pricingInfo = productValidationService.validateAndGetProductPricing(
                item.getProductId(), item.getVariantId());

            item.setQuantity(quantity);
            updateCartItemPricing(item, pricingInfo);
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart setShippingAddress(UUID userId, UUID addressId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        cart.setShippingAddressId(addressId);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart setBillingAddress(UUID userId, UUID addressId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        cart.setBillingAddressId(addressId);
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Optional<Cart> getCartByUserId(UUID userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    public Cart copyItemsFromOrder(UUID userId, UUID orderId) {
        // TODO: This will be implemented when Order domain is ready
        // For now, return existing cart or create new one
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setItems(new java.util.ArrayList<>());
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    public void mergeGuestCart(UUID guestCartId, UUID userId) {
        Optional<Cart> guestCart = cartRepository.findById(guestCartId);
        if (guestCart.isEmpty()) {
            return; // Guest cart not found, nothing to merge
        }

        Cart userCart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setItems(new java.util.ArrayList<>());
            return newCart;
        });

        // Merge items from guest cart to user cart
        if (guestCart.get().getItems() != null) {
            if (userCart.getItems() == null) {
                userCart.setItems(new java.util.ArrayList<>());
            }
            for (CartItem guestItem : guestCart.get().getItems()) {
                // Re-validate and update pricing for merged items
                try {
                    ProductPricingInfo pricingInfo = productValidationService.validateAndGetProductPricing(
                        guestItem.getProductId(), guestItem.getVariantId());

                    updateCartItemPricing(guestItem, pricingInfo);
                    guestItem.setCart(userCart);
                    userCart.getItems().add(guestItem);
                } catch (ProductValidationException e) {
                    log.warn("Skipping invalid product during cart merge: {}", e.getMessage());
                    // Skip invalid products during merge
                }
            }
        }

        // Save merged cart and delete guest cart
        cartRepository.save(userCart);
        cartRepository.delete(guestCart.get());
    }

    @Transactional
    public void convertCartToOrder(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (cart.getShippingAddressId() == null || cart.getBillingAddressId() == null) {
            throw new RuntimeException("Shipping and billing addresses must be set before checkout");
        }

        // Re-validate all cart items before conversion
        validateCartItemsBeforeCheckout(cart);

        // Use OrderService to create order from cart
        orderService.createOrderFromCart(userId, cart.getShippingAddressId(), cart.getBillingAddressId());
    }

    /**
     * Validate all cart items before checkout to ensure products are still valid and prices are current
     */
    private void validateCartItemsBeforeCheckout(Cart cart) {
        log.info("Validating cart items before checkout for cart: {}", cart.getId());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            try {
                // Re-validate product and get current pricing
                ProductPricingInfo currentPricing = productValidationService.validateAndGetProductPricing(
                    item.getProductId(), item.getVariantId());

                // Validate stock availability
                if (!productValidationService.validateStockAvailability(
                    item.getProductId(), item.getVariantId(), item.getQuantity())) {
                    throw new ProductValidationException(
                        "Insufficient stock for product: " + currentPricing.getProductName());
                }

                // Update item with current pricing
                updateCartItemPricing(item, currentPricing);

            } catch (ProductValidationException e) {
                throw new RuntimeException("Checkout validation failed for product in cart: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Create a new cart item with validated product information
     */
    private CartItem createCartItem(Cart cart, ProductPricingInfo pricingInfo, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(pricingInfo.getProductId());
        item.setVariantId(pricingInfo.getVariantId());
        item.setQuantity(quantity);

        updateCartItemPricing(item, pricingInfo);

        return item;
    }

    /**
     * Update cart item with current pricing information
     */
    private void updateCartItemPricing(CartItem item, ProductPricingInfo pricingInfo) {
        item.setUnitPrice(pricingInfo.getPrice());
        item.setProductName(pricingInfo.getProductName());
        item.setVariantName(pricingInfo.getVariantName());
        item.setSku(pricingInfo.getSku());

        // Calculate total price for the item
        BigDecimal totalPrice = pricingInfo.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setTotalPrice(totalPrice);

        // Set other pricing fields (can be extended for discounts, taxes, etc.)
        item.setDiscountAmount(BigDecimal.ZERO); // No discount for now
        item.setTaxAmount(BigDecimal.ZERO); // No tax calculation for now
    }
}
