package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.AnalyticsResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.AnalyticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestParam @Min(2000) int year,
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) int months,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(analyticsService.getAnalytics(user, year, month, months));
    }
}
