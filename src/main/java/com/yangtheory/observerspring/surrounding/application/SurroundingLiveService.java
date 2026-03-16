package com.yangtheory.observerspring.surrounding.application;

import com.yangtheory.observerspring.surrounding.domain.LayerType;
import com.yangtheory.observerspring.surrounding.dto.ActivityLogResponse;
import com.yangtheory.observerspring.surrounding.dto.BlockLogIngestRequest;
import com.yangtheory.observerspring.surrounding.dto.PlayerSnapshotIngestRequest;
import com.yangtheory.observerspring.surrounding.dto.PlayerSummaryResponse;
import com.yangtheory.observerspring.surrounding.dto.PlayerSurroundingsResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Service;

@Service
public class SurroundingLiveService {
    private static final int MAX_ACTIVITY_LOGS = 400;

    private final Map<String, StoredSnapshot> latestByUuid = new ConcurrentHashMap<>();
    private final Map<String, String> uuidByPlayerName = new ConcurrentHashMap<>();
    private final Deque<ActivityLogResponse> activityLogs = new ConcurrentLinkedDeque<>();

    public void ingestSnapshot(PlayerSnapshotIngestRequest request) {
        OffsetDateTime capturedAt = parseTimestamp(request.capturedAt());
        List<PlayerSurroundingsResponse.Layer> layers = request.layers().stream()
                .map(this::mapLayer)
                .toList();
        List<PlayerSurroundingsResponse.Highlight> highlights = collectHighlights(layers);

        StoredSnapshot snapshot = new StoredSnapshot(
                request.playerUuid(),
                request.playerName(),
                normalize(request.playerName()),
                request.serverName(),
                request.worldName(),
                request.dimensionType(),
                request.centerX(),
                request.centerY(),
                request.centerZ(),
                request.yaw(),
                request.pitch(),
                request.radius(),
                request.capturedAt(),
                capturedAt,
                layers,
                highlights
        );

        StoredSnapshot previous = latestByUuid.put(request.playerUuid(), snapshot);
        if (previous != null && !previous.playerNameKey().equals(snapshot.playerNameKey())) {
            uuidByPlayerName.remove(previous.playerNameKey());
        }
        uuidByPlayerName.put(snapshot.playerNameKey(), snapshot.playerUuid());

        String message = highlights.isEmpty()
                ? "%s 위치 스냅샷이 갱신되었습니다."
                : "%s 주변에서 강조 블록 %d개가 감지되었습니다."
                        .formatted(snapshot.playerName(), highlights.size());

        appendLog(new ActivityLogResponse(
                "SNAPSHOT",
                snapshot.playerName(),
                snapshot.worldName(),
                message,
                snapshot.capturedAtRaw()
        ));
    }

    public void ingestBlockLog(BlockLogIngestRequest request) {
        String message = "%s %s (%d, %d, %d)"
                .formatted(request.action(), request.blockType(), request.x(), request.y(), request.z());
        appendLog(new ActivityLogResponse(
                "BLOCK",
                request.player(),
                request.world(),
                message,
                request.timestamp()
        ));
    }

    public List<PlayerSummaryResponse> listPlayers() {
        return latestByUuid.values().stream()
                .sorted(Comparator.comparing(StoredSnapshot::capturedAt).reversed())
                .map(StoredSnapshot::toSummary)
                .toList();
    }

    public Optional<PlayerSurroundingsResponse> findSurroundings(String playerName) {
        String uuid = uuidByPlayerName.get(normalize(playerName));
        if (uuid == null) {
            return Optional.empty();
        }

        StoredSnapshot snapshot = latestByUuid.get(uuid);
        if (snapshot == null) {
            return Optional.empty();
        }

        return Optional.of(snapshot.toResponse());
    }

    public List<ActivityLogResponse> getActivityLogs(String playerName, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String normalizedPlayer = normalize(playerName);
        List<ActivityLogResponse> result = new ArrayList<>(safeLimit);

        for (ActivityLogResponse log : activityLogs) {
            if (normalizedPlayer != null && !Objects.equals(normalize(log.playerName()), normalizedPlayer)) {
                continue;
            }

            result.add(log);
            if (result.size() >= safeLimit) {
                break;
            }
        }

        return result;
    }

    private PlayerSurroundingsResponse.Layer mapLayer(PlayerSnapshotIngestRequest.LayerRequest request) {
        List<List<PlayerSurroundingsResponse.Cell>> rows = request.rows().stream()
                .map(row -> row.stream()
                        .map(cell -> new PlayerSurroundingsResponse.Cell(
                                cell.material(),
                                cell.blockX(),
                                cell.blockY(),
                                cell.blockZ(),
                                cell.highlighted(),
                                cell.loaded()
                        ))
                        .toList())
                .toList();

        return new PlayerSurroundingsResponse.Layer(
                request.type(),
                toLabel(request.type()),
                request.relativeY(),
                request.absoluteY(),
                rows
        );
    }

    private List<PlayerSurroundingsResponse.Highlight> collectHighlights(List<PlayerSurroundingsResponse.Layer> layers) {
        Map<String, PlayerSurroundingsResponse.Highlight> highlights = new LinkedHashMap<>();

        for (PlayerSurroundingsResponse.Layer layer : layers) {
            for (List<PlayerSurroundingsResponse.Cell> row : layer.rows()) {
                for (PlayerSurroundingsResponse.Cell cell : row) {
                    if (!cell.highlighted()) {
                        continue;
                    }

                    String key = cell.blockX() + ":" + cell.blockY() + ":" + cell.blockZ();
                    highlights.putIfAbsent(key, new PlayerSurroundingsResponse.Highlight(
                            cell.material(),
                            cell.blockX(),
                            cell.blockY(),
                            cell.blockZ(),
                            layer.type()
                    ));
                }
            }
        }

        return List.copyOf(highlights.values());
    }

    private void appendLog(ActivityLogResponse log) {
        activityLogs.addFirst(log);
        while (activityLogs.size() > MAX_ACTIVITY_LOGS) {
            activityLogs.pollLast();
        }
    }

    private OffsetDateTime parseTimestamp(String value) {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    private String toLabel(LayerType type) {
        return switch (type) {
            case FEET -> "발밑";
            case BODY -> "몸높이";
            case HEAD -> "머리위";
        };
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private record StoredSnapshot(
            String playerUuid,
            String playerName,
            String playerNameKey,
            String serverName,
            String worldName,
            String dimensionType,
            int centerX,
            int centerY,
            int centerZ,
            float yaw,
            float pitch,
            int radius,
            String capturedAtRaw,
            OffsetDateTime capturedAt,
            List<PlayerSurroundingsResponse.Layer> layers,
            List<PlayerSurroundingsResponse.Highlight> highlights
    ) {
        private PlayerSummaryResponse toSummary() {
            return new PlayerSummaryResponse(
                    playerUuid,
                    playerName,
                    serverName,
                    worldName,
                    dimensionType,
                    centerX,
                    centerY,
                    centerZ,
                    capturedAtRaw,
                    highlights.size()
            );
        }

        private PlayerSurroundingsResponse toResponse() {
            return new PlayerSurroundingsResponse(
                    playerUuid,
                    playerName,
                    serverName,
                    worldName,
                    dimensionType,
                    centerX,
                    centerY,
                    centerZ,
                    yaw,
                    pitch,
                    radius,
                    capturedAtRaw,
                    layers,
                    highlights
            );
        }
    }
}
