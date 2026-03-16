package com.yangtheory.observerspring.surrounding.api;

import com.yangtheory.observerspring.surrounding.application.SurroundingLiveService;
import com.yangtheory.observerspring.surrounding.dto.ActivityLogResponse;
import com.yangtheory.observerspring.surrounding.dto.BlockLogIngestRequest;
import com.yangtheory.observerspring.surrounding.dto.PlayerSnapshotIngestRequest;
import com.yangtheory.observerspring.surrounding.dto.PlayerSummaryResponse;
import com.yangtheory.observerspring.surrounding.dto.PlayerSurroundingsResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SurroundingController {
    private final SurroundingLiveService surroundingLiveService;

    public SurroundingController(SurroundingLiveService surroundingLiveService) {
        this.surroundingLiveService = surroundingLiveService;
    }

    @PostMapping("/player-snapshots")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestSnapshot(@Valid @RequestBody PlayerSnapshotIngestRequest request) {
        surroundingLiveService.ingestSnapshot(request);
    }

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestLog(@Valid @RequestBody BlockLogIngestRequest request) {
        surroundingLiveService.ingestBlockLog(request);
    }

    @GetMapping("/players")
    public List<PlayerSummaryResponse> listPlayers() {
        return surroundingLiveService.listPlayers();
    }

    @GetMapping("/players/{name}/surroundings")
    public PlayerSurroundingsResponse getSurroundings(@PathVariable String name) {
        return surroundingLiveService.findSurroundings(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player snapshot not found"));
    }

    @GetMapping("/logs")
    public List<ActivityLogResponse> getLogs(
            @RequestParam(required = false) String player,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return surroundingLiveService.getActivityLogs(player, limit);
    }
}
