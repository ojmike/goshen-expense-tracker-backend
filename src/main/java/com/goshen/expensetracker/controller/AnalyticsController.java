package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.AnalyticsResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "6") int months,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(analyticsService.getAnalytics(user, year, month, months));
    }
}
