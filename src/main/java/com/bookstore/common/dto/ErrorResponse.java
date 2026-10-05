package com.bookstore.common.dto;

import java.util.List;

public class ErrorResponse {

    private String code;
    private String message;
    private List<String> details;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<String> getDetails() { return details; }
}
