package com.yangtheory.observerspring.surrounding.dto;

import com.yangtheory.observerspring.surrounding.domain.LayerType;
import java.util.List;

public record PlayerSurroundingsResponse(
        String playerUuid,
        String playerName,
        String serverName,
        String worldName,
        String dimensionType,
        int centerX,
        int centerY,
        int centerZ,
        float yaw,
        float pitch,
        int radius,
        String capturedAt,
        List<Layer> layers,
        List<Highlight> highlights
) {
    public record Layer(
            LayerType type,
            String label,
            int relativeY,
            int absoluteY,
            List<List<Cell>> rows
    ) {
    }

    public record Cell(
            String material,
            int blockX,
            int blockY,
            int blockZ,
            boolean highlighted,
            boolean loaded
    ) {
    }

    public record Highlight(
            String material,
            int blockX,
            int blockY,
            int blockZ,
            LayerType layerType
    ) {
    }
}
