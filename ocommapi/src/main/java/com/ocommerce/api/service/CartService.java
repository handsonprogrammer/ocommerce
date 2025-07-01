package com.ocommerce.api.service;

import com.ocommerce.api.constants.CartStatus;
import com.ocommerce.api.exception.ProductNotFoundException;
import com.ocommerce.api.jpa.entities.Cart;
import com.ocommerce.api.jpa.entities.CartItem;
import com.ocommerce.api.jpa.entities.Product;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.CartItemsRepository;
import com.ocommerce.api.jpa.repositories.CartRepository;
import com.ocommerce.api.jpa.repositories.ProductRepository;
import com.ocommerce.api.jpa.repositories.UserRegRepository;
import com.ocommerce.api.mapper.CartMapper;
import com.ocommerce.api.model.AddToCartRequest;
import com.ocommerce.api.model.CartDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRegRepository userRegRepository;
    private final CartItemsRepository cartItemsRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            UserRegRepository userRegRepository, CartItemsRepository cartItemsRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRegRepository = userRegRepository;
        this.cartItemsRepository = cartItemsRepository;
    }

    @Transactional
    public CartDto addItemsToCart(Long userId, AddToCartRequest request) throws ProductNotFoundException {
        UserReg user = userRegRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get or create an active cart for the user
        List<Cart> activeCarts = cartRepository.findActiveCartsByUserId(userId);
        Cart cart;
        if (activeCarts.isEmpty()) {
            cart = new Cart();
            cart.setUser(user);
            cart.setStatus(CartStatus.ACTIVE);
        } else {
            cart = activeCarts.get(0);
        }

        // Find product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(
                        () -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cart.getCartItems().add(cartItem);
        }

        cart = cartRepository.save(cart);
        return CartMapper.toDto(cart);
    }

    @Transactional
    public CartItem updateCartItem(Long cartItemId, int quantity, Long userId) {

        CartItem cartItem = cartItemsRepository.findByCartItemsIdAndUserId(cartItemId, userId);

        if (cartItem != null) {
            Cart cart = cartItem.getCart();
            if (quantity > 0) {
                cartItem.setQuantity(quantity);
            } else {
                // Remove item if quantity is zero or less
                cart.getCartItems().remove(cartItem);
            }
            return cartItemsRepository.save(cartItem);
        } else {
            throw new IllegalArgumentException("Cart item not found");
        }
    }

}