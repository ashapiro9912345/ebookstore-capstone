package com.bookstore.common.exception;

/**
 * Thrown when an operation violates a business rule.
 * Maps to HTTP 422 Unprocessable Entity in {@link GlobalExceptionHandler}.
 *
 * Examples:
 * - Cart is empty at checkout
 * - Insufficient stock
 * - Simulated payment declined
 * - 48-hour cancellation window has expired
 * - Attempting to cancel an already-cancelled order
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
