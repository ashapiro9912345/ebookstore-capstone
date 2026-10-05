package com.bookstore.user.dto;

import java.time.OffsetDateTime;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private OffsetDateTime createdAt;

    public UserResponse(Long id, String fullName, String email, OffsetDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
