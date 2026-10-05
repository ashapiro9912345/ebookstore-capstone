package com.bookstore.cart.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a single cart line item.
 * Matches the OpenAPI CartItemResponse schema.
 *
 * lineTotal = price × quantity, computed in the service layer.
 */
public class CartItemResponse {

    private Long itemId;
    private Long bookId;
    private String title;
    private String author;
    private BigDecimal price;
    private int quantity;
    private BigDecimal lineTotal;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CartItemResponse() {}

    public CartItemResponse(Long itemId, Long bookId, String title, String author,
                            BigDecimal price, int quantity, BigDecimal lineTotal) {
        this.itemId = itemId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getItemId() { return itemId; }
    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
