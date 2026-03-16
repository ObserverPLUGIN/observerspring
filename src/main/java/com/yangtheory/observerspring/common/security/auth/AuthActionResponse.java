package com.yangtheory.observerspring.common.security.auth;

public record AuthActionResponse(
        String message,
        String email,
        String nextStep,
        String debugCode
) {
}
