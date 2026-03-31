package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.IncomeOverviewResponse;
import com.goshen.expensetracker.model.dto.IncomeSourceRequest;
import com.goshen.expensetracker.model.dto.IncomeSourceResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.IncomeSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeSourceController {

    private final IncomeSourceService incomeSourceService;

    @GetMapping
    public ResponseEntity<IncomeOverviewResponse> getOverview(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(incomeSourceService.getOverview(user));
    }

    @PostMapping
    public ResponseEntity<IncomeSourceResponse> create(
            @Valid @RequestBody IncomeSourceRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incomeSourceService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeSourceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IncomeSourceRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(incomeSourceService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        incomeSourceService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
