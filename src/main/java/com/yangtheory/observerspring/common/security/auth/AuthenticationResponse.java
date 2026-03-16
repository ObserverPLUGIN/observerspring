package com.yangtheory.observerspring.common.security.auth;

public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        String username
) {
}
