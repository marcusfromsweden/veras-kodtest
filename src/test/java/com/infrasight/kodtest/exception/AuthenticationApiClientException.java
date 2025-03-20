package com.infrasight.kodtest.exception;

public class AuthenticationApiClientException extends RuntimeException {
    public AuthenticationApiClientException(String message) {
        super(message);
    }

    public AuthenticationApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
