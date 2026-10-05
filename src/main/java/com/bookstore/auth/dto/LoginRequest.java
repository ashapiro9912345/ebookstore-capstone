package com.bookstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/login.
 */
public class LoginRequest {

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
