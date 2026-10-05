package com.bookstore.catalogue.dto;

/**
 * Response DTO for a book category.
 * Matches the OpenAPI CategoryResponse schema.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
