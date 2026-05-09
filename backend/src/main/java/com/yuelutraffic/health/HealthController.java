package com.yuelutraffic.health;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "yuelu-traffic-backend",
                "time", OffsetDateTime.now().toString());
    }
}
