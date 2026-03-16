package com.yangtheory.observerspring.common.security.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
