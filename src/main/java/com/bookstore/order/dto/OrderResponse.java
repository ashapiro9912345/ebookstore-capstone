package com.bookstore.order.dto;

import com.bookstore.order.OrderStatus;
import com.bookstore.order.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Full order response DTO.
 * Matches the OpenAPI OrderResponse schema.
 *
 * simulatedPaymentReference is a generated reference code only — no real
 * payment token or card credential is included here.
 */
public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String simulatedPaymentReference;   // nullable
    private ShippingAddressSnapshot shippingAddress;
    private List<OrderItemResponse> items;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public OrderResponse() {}

    public OrderResponse(Long id, OrderStatus status, BigDecimal totalAmount,
                         PaymentMethod paymentMethod, String simulatedPaymentReference,
                         ShippingAddressSnapshot shippingAddress,
                         List<OrderItemResponse> items,
                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.status = status;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.simulatedPaymentReference = simulatedPaymentReference;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getSimulatedPaymentReference() { return simulatedPaymentReference; }
    public ShippingAddressSnapshot getShippingAddress() { return shippingAddress; }
    public List<OrderItemResponse> getItems() { return items; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
