package com.bookstore.catalogue.dto;

import java.math.BigDecimal;

/**
 * Full book detail DTO returned by GET /api/books/{bookId}.
 * Matches the OpenAPI BookDetailResponse schema.
 *
 * Monetary value (price) uses BigDecimal to avoid floating-point imprecision.
 */
public class BookDetailResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;          // nullable
    private String description;   // nullable
    private BigDecimal price;
    private int stockQuantity;
    private int deliveryEstimateDays;
    private CategoryResponse category;
    private BrandResponse brand;  // nullable

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BookDetailResponse() {}

    public BookDetailResponse(Long id, String title, String author,
                              String isbn, String description,
                              BigDecimal price, int stockQuantity, int deliveryEstimateDays,
                              CategoryResponse category, BrandResponse brand) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.deliveryEstimateDays = deliveryEstimateDays;
        this.category = category;
        this.brand = brand;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public int getDeliveryEstimateDays() { return deliveryEstimateDays; }
    public CategoryResponse getCategory() { return category; }
    public BrandResponse getBrand() { return brand; }
}
