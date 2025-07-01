package com.ocommerce.api.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class CartDto {
    private Long cartId;
    private String status;
    private Long userId;
    private Long shippingAddressId;
    private Long billingAddressId;
    private BigDecimal totalAmount;
    private BigDecimal totalTax;
    private BigDecimal totalShippingCost;
    private boolean locked;
    private Date createAt;
    private Date editedAt;
    private List<CartItemDto> cartItems;

    // Getters and setters

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public Long getBillingAddressId() { return billingAddressId; }
    public void setBillingAddressId(Long billingAddressId) { this.billingAddressId = billingAddressId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }

    public BigDecimal getTotalShippingCost() { return totalShippingCost; }
    public void setTotalShippingCost(BigDecimal totalShippingCost) { this.totalShippingCost = totalShippingCost; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date getEditedAt() { return editedAt; }
    public void setEditedAt(Date editedAt) { this.editedAt = editedAt; }

    public List<CartItemDto> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDto> cartItems) { this.cartItems = cartItems; }

    public static class CartItemDto {
        private Long cartItemId;
        private Long productId;
        private int quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
        private BigDecimal tax;
        private BigDecimal shippingCost;
        private BigDecimal totalAmount;
        private BigDecimal discount;
        private Long userId;
        private Date createAt;
        private Date updatedAt;

        // Getters and setters
        public Long getCartItemId() { return cartItemId; }
        public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }

        public BigDecimal getShippingCost() { return shippingCost; }
        public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Date getCreateAt() { return createAt; }
        public void setCreateAt(Date createAt) { this.createAt = createAt; }

        public Date getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    }
}