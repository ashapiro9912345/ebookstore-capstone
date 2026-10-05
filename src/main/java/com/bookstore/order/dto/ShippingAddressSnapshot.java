package com.bookstore.order.dto;

/**
 * Embedded snapshot of the delivery address captured at order placement time.
 * Matches the OpenAPI ShippingAddressSnapshot schema.
 *
 * This DTO is populated from the snap_* columns on the orders table (DM-03),
 * ensuring that historical order data is preserved even if the customer later
 * edits or deletes the original address record.
 */
public class ShippingAddressSnapshot {

    private String recipientName;
    private String streetLine1;
    private String streetLine2;  // nullable
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ShippingAddressSnapshot() {}

    public ShippingAddressSnapshot(String recipientName, String streetLine1,
                                   String streetLine2, String city, String state,
                                   String postalCode, String country) {
        this.recipientName = recipientName;
        this.streetLine1 = streetLine1;
        this.streetLine2 = streetLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getRecipientName() { return recipientName; }
    public String getStreetLine1() { return streetLine1; }
    public String getStreetLine2() { return streetLine2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
}
