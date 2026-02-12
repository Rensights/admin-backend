package com.rensights.admin.controller;

import com.rensights.admin.service.WeeklyDealsSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/weekly-deals")
@RequiredArgsConstructor
public class WeeklyDealsSettingsController {

    private final WeeklyDealsSettingsService weeklyDealsSettingsService;

    @PutMapping("/enable")
    public ResponseEntity<?> setEnabled(@RequestParam boolean enabled) {
        return ResponseEntity.ok(java.util.Map.of("enabled", weeklyDealsSettingsService.setWeeklyDealsEnabled(enabled)));
    }

    @GetMapping("/enable")
    public ResponseEntity<?> getEnabled() {
        return ResponseEntity.ok(java.util.Map.of("enabled", weeklyDealsSettingsService.isWeeklyDealsEnabled()));
    }
}
