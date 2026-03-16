package com.yangtheory.observerspring.common.security.auth;

public record CurrentUserResponse(
        String username,
        String email,
        String displayName,
        String role
) {
}
