package com.bookstore.cart.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the customer's shopping cart.
 * Matches the OpenAPI CartResponse schema.
 *
 * totalAmount = sum of all line totals, computed in the service layer.
 */
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CartResponse() {}

    public CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal totalAmount) {
        this.cartId = cartId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getCartId() { return cartId; }
    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
