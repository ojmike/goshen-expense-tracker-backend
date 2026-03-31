package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.ExpenseRequest;
import com.goshen.expensetracker.model.dto.ExpenseResponse;
import com.goshen.expensetracker.model.dto.MonthlyExpenseOverview;
import java.util.List;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<MonthlyExpenseOverview> getMonthly(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(expenseService.getMonthlyOverview(user, year, month));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(expenseService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        expenseService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copy-previous")
    public ResponseEntity<List<ExpenseResponse>> copyFromPreviousMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.copyFromPreviousMonth(user, year, month));
    }
}
