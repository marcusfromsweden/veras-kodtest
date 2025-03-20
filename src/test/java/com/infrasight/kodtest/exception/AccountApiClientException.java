package com.infrasight.kodtest.exception;

public class AccountApiClientException extends RuntimeException {

    public AccountApiClientException(String message) {
        super(message);
    }

    public AccountApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
