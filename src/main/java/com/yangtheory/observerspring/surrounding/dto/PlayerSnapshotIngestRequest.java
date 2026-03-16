package com.yangtheory.observerspring.surrounding.dto;

import com.yangtheory.observerspring.surrounding.domain.LayerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlayerSnapshotIngestRequest(
        @NotBlank String serverName,
        @NotBlank String playerUuid,
        @NotBlank String playerName,
        @NotBlank String worldName,
        @NotBlank String dimensionType,
        int centerX,
        int centerY,
        int centerZ,
        float yaw,
        float pitch,
        @Min(1) int radius,
        @NotBlank String capturedAt,
        @NotEmpty List<@Valid LayerRequest> layers
) {
    public record LayerRequest(
            @NotNull LayerType type,
            int relativeY,
            int absoluteY,
            @NotEmpty List<List<@Valid CellRequest>> rows
    ) {
    }

    public record CellRequest(
            @NotBlank String material,
            int blockX,
            int blockY,
            int blockZ,
            boolean highlighted,
            boolean loaded
    ) {
    }
}
