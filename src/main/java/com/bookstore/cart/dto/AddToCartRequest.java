package com.bookstore.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/cart/items.
 * Constraints match the OpenAPI AddToCartRequest schema.
 */
public class AddToCartRequest {

    @NotNull(message = "bookId is required")
    private Long bookId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AddToCartRequest() {}

    public AddToCartRequest(Long bookId, Integer quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
