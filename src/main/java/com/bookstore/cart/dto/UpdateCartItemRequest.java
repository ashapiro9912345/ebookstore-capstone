package com.bookstore.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for PUT /api/cart/items/{itemId}.
 * Constraints match the OpenAPI UpdateCartItemRequest schema.
 */
public class UpdateCartItemRequest {

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UpdateCartItemRequest() {}

    public UpdateCartItemRequest(Integer quantity) {
        this.quantity = quantity;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
