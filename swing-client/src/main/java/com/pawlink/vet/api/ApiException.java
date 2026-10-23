package com.pawlink.vet.api;

/**
 * Custom RuntimeException thrown when an API request returns an HTTP error status code.
 * Contains the HTTP status code and server error message.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "ApiException [HTTP " + statusCode + "]: " + getMessage();
    }
}
