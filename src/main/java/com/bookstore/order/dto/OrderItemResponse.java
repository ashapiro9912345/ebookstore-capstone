package com.bookstore.order.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a single order line item.
 * Matches the OpenAPI OrderItemResponse schema.
 *
 * unitPrice is the price locked at checkout time (BR-06 — historical price preserved).
 * lineTotal = unitPrice × quantity, computed in the service layer.
 */
public class OrderItemResponse {

    private Long bookId;
    private String title;
    private String author;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public OrderItemResponse() {}

    public OrderItemResponse(Long bookId, String title, String author,
                             int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
