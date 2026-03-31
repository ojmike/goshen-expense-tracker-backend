package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.CashFlowResponse;
import com.goshen.expensetracker.model.dto.DashboardResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.CashFlowService;
import com.goshen.expensetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CashFlowService cashFlowService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getOverview(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        validateYearMonth(year, month);
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(dashboardService.getOverview(user, year, month));
    }

    @GetMapping("/cashflow")
    public ResponseEntity<CashFlowResponse> getCashFlow(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        validateYearMonth(year, month);
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cashFlowService.getCashFlow(user, year, month));
    }

    private void validateYearMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("year is out of supported range");
        }
    }
}
