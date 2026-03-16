package com.yangtheory.observerspring.common.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "observerspring",
                "timestamp", OffsetDateTime.now().toString(),
                "features", List.of("react", "spring-boot", "minecraft-observer"));
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "Observer API",
                "message", "React frontend, Spring Boot dashboard API, and Minecraft snapshot ingest are ready.",
                "endpoints", List.of(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/email/request",
                        "/api/auth/email/verify",
                        "/api/auth/password-reset/request",
                        "/api/auth/password-reset/confirm",
                        "/api/health",
                        "/api/players",
                        "/api/players/{name}/surroundings",
                        "/api/logs",
                        "/api/player-snapshots",
                        "/actuator/health"
                ));
    }
}
