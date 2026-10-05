package com.bookstore.catalogue.dto;

import java.math.BigDecimal;

/**
 * Lightweight book DTO used in catalogue listings and search results.
 * Matches the OpenAPI BookSummaryResponse schema.
 *
 * Monetary value (price) uses BigDecimal to avoid floating-point imprecision.
 */
public class BookSummaryResponse {

    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private int stockQuantity;
    private int deliveryEstimateDays;
    private Long categoryId;
    private String categoryName;
    private Long brandId;       // nullable
    private String brandName;   // nullable

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BookSummaryResponse() {}

    public BookSummaryResponse(Long id, String title, String author, BigDecimal price,
                               int stockQuantity, int deliveryEstimateDays,
                               Long categoryId, String categoryName,
                               Long brandId, String brandName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.deliveryEstimateDays = deliveryEstimateDays;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public int getDeliveryEstimateDays() { return deliveryEstimateDays; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Long getBrandId() { return brandId; }
    public String getBrandName() { return brandName; }
}
