package com.bookstore.catalogue.dto;

/**
 * Response DTO for a brand/publisher.
 * Matches the OpenAPI BrandResponse schema.
 */
public class BrandResponse {

    private Long id;
    private String name;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BrandResponse() {}

    public BrandResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getName() { return name; }
}
