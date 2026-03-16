package com.yangtheory.observerspring.surrounding.dto;

import jakarta.validation.constraints.NotBlank;

public record BlockLogIngestRequest(
        @NotBlank String player,
        @NotBlank String world,
        int x,
        int y,
        int z,
        @NotBlank String blockType,
        @NotBlank String timestamp,
        @NotBlank String action
) {
}
