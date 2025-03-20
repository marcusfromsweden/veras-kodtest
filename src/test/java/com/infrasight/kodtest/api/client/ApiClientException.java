package com.infrasight.kodtest.api.client;

/**
 * Custom exception for API client errors.
 */
public class ApiClientException extends RuntimeException {

    /**
     * Constructs a new ApiClientException with a specific error message.
     *
     * @param message The error message describing the issue.
     */
    public ApiClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new ApiClientException with a message and the underlying cause.
     *
     * @param message The error message describing the issue.
     * @param cause   The original exception that caused this error.
     */
    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
