package com.rensights.admin.controller;

import com.rensights.admin.service.GoogleAnalyticsSettingsService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings/google-analytics")
@RequiredArgsConstructor
public class GoogleAnalyticsSettingsController {

    private final GoogleAnalyticsSettingsService googleAnalyticsSettingsService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getMeasurementId() {
        return ResponseEntity.ok(
            Map.of("measurementId", googleAnalyticsSettingsService.getMeasurementId())
        );
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> setMeasurementId(
        @RequestBody Map<String, String> body
    ) {
        String value = body == null ? "" : body.getOrDefault("measurementId", "");
        return ResponseEntity.ok(
            Map.of("measurementId", googleAnalyticsSettingsService.setMeasurementId(value))
        );
    }
}
