package com.bookstore.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/addresses.
 * Constraints match the OpenAPI AddressRequest schema.
 */
public class AddressRequest {

    @NotBlank(message = "recipientName is required")
    @Size(max = 255, message = "recipientName must not exceed 255 characters")
    private String recipientName;

    @NotBlank(message = "streetLine1 is required")
    @Size(max = 255, message = "streetLine1 must not exceed 255 characters")
    private String streetLine1;

    @Size(max = 255, message = "streetLine2 must not exceed 255 characters")
    private String streetLine2;

    @NotBlank(message = "city is required")
    @Size(max = 100, message = "city must not exceed 100 characters")
    private String city;

    @NotBlank(message = "state is required")
    @Size(max = 100, message = "state must not exceed 100 characters")
    private String state;

    @NotBlank(message = "postalCode is required")
    @Size(max = 20, message = "postalCode must not exceed 20 characters")
    private String postalCode;

    @NotBlank(message = "country is required")
    @Size(max = 100, message = "country must not exceed 100 characters")
    private String country;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AddressRequest() {}

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getStreetLine1() { return streetLine1; }
    public void setStreetLine1(String streetLine1) { this.streetLine1 = streetLine1; }

    public String getStreetLine2() { return streetLine2; }
    public void setStreetLine2(String streetLine2) { this.streetLine2 = streetLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
