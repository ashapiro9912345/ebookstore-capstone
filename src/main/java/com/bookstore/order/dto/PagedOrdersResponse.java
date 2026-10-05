package com.bookstore.order.dto;

import java.util.List;

/**
 * Paginated order history response.
 * Matches the OpenAPI PagedOrdersResponse schema.
 */
public class PagedOrdersResponse {

    private List<OrderResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public PagedOrdersResponse() {}

    public PagedOrdersResponse(List<OrderResponse> content, int page, int size,
                               long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<OrderResponse> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
