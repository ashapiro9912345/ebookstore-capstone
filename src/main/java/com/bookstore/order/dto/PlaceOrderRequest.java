package com.bookstore.order.dto;

import com.bookstore.order.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/orders (checkout).
 *
 * Only the minimum non-sensitive information is accepted:
 *   - addressId  — which of the customer's saved addresses to ship to
 *   - paymentMethod — CREDIT_CARD or DEBIT_CARD (simulated; no card credentials stored)
 *
 * No card numbers, CVVs, or real payment tokens are accepted or stored.
 */
public class PlaceOrderRequest {

    @NotNull(message = "addressId is required")
    private Long addressId;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public PlaceOrderRequest() {}

    public PlaceOrderRequest(Long addressId, PaymentMethod paymentMethod) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
