package com.yangtheory.observerspring.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

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
                "message", "React frontend and Spring Boot backend are ready.",
                "endpoints", List.of("/api/health", "/actuator/health"));
    }
}
