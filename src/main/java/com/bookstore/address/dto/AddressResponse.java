package com.bookstore.address.dto;

import java.time.OffsetDateTime;

/**
 * Response DTO for a customer delivery address.
 * Matches the OpenAPI AddressResponse schema.
 */
public class AddressResponse {

    private Long id;
    private String recipientName;
    private String streetLine1;
    private String streetLine2;  // nullable
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AddressResponse() {}

    public AddressResponse(Long id, String recipientName, String streetLine1,
                           String streetLine2, String city, String state,
                           String postalCode, String country, OffsetDateTime createdAt) {
        this.id = id;
        this.recipientName = recipientName;
        this.streetLine1 = streetLine1;
        this.streetLine2 = streetLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getRecipientName() { return recipientName; }
    public String getStreetLine1() { return streetLine1; }
    public String getStreetLine2() { return streetLine2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
