package com.yangtheory.observerspring.common.security.auth;

public class InvalidEmailCodeException extends RuntimeException {
    public InvalidEmailCodeException(String message) {
        super(message);
    }
}
