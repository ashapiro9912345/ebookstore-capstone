package com.bookstore.catalogue.dto;

import java.util.List;

/**
 * Paginated book listing response.
 * Matches the OpenAPI PagedBooksResponse schema.
 *
 * Wraps Spring Data's Page result into a simple, stable DTO so that
 * the response shape is not coupled to Spring's internal Page structure.
 */
public class PagedBooksResponse {

    private List<BookSummaryResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public PagedBooksResponse() {}

    public PagedBooksResponse(List<BookSummaryResponse> content, int page, int size,
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

    public List<BookSummaryResponse> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}
