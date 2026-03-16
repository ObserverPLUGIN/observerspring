package com.yangtheory.observerspring.surrounding.dto;

public record PlayerSummaryResponse(
        String playerUuid,
        String playerName,
        String serverName,
        String worldName,
        String dimensionType,
        int centerX,
        int centerY,
        int centerZ,
        String capturedAt,
        int highlightCount
) {
}
