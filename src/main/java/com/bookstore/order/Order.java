package com.bookstore.order;

import com.bookstore.address.Address;
import com.bookstore.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "simulated_payment_reference", length = 100)
    private String simulatedPaymentReference;

    // Nullable FK to addresses — snapshot columns are authoritative (DM-03)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    // Address snapshot columns (DM-03)
    @Column(name = "snap_recipient", nullable = false, length = 255)
    private String snapRecipient;

    @Column(name = "snap_street_line1", nullable = false, length = 255)
    private String snapStreetLine1;

    @Column(name = "snap_street_line2", length = 255)
    private String snapStreetLine2;

    @Column(name = "snap_city", nullable = false, length = 100)
    private String snapCity;

    @Column(name = "snap_state", nullable = false, length = 100)
    private String snapState;

    @Column(name = "snap_postal_code", nullable = false, length = 20)
    private String snapPostalCode;

    @Column(name = "snap_country", nullable = false, length = 100)
    private String snapCountry;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSimulatedPaymentReference() { return simulatedPaymentReference; }
    public void setSimulatedPaymentReference(String simulatedPaymentReference) {
        this.simulatedPaymentReference = simulatedPaymentReference;
    }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getSnapRecipient() { return snapRecipient; }
    public void setSnapRecipient(String snapRecipient) { this.snapRecipient = snapRecipient; }

    public String getSnapStreetLine1() { return snapStreetLine1; }
    public void setSnapStreetLine1(String snapStreetLine1) { this.snapStreetLine1 = snapStreetLine1; }

    public String getSnapStreetLine2() { return snapStreetLine2; }
    public void setSnapStreetLine2(String snapStreetLine2) { this.snapStreetLine2 = snapStreetLine2; }

    public String getSnapCity() { return snapCity; }
    public void setSnapCity(String snapCity) { this.snapCity = snapCity; }

    public String getSnapState() { return snapState; }
    public void setSnapState(String snapState) { this.snapState = snapState; }

    public String getSnapPostalCode() { return snapPostalCode; }
    public void setSnapPostalCode(String snapPostalCode) { this.snapPostalCode = snapPostalCode; }

    public String getSnapCountry() { return snapCountry; }
    public void setSnapCountry(String snapCountry) { this.snapCountry = snapCountry; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
