package com.yangtheory.observerspring.surrounding.dto;

public record ActivityLogResponse(
        String type,
        String playerName,
        String worldName,
        String message,
        String timestamp
) {
}
