package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.*;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(loanService.getAllLoans(user));
    }

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(
            @Valid @RequestBody LoanRequest request,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(request, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDetailResponse> getLoanDetail(
            @PathVariable Long id,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(loanService.getLoanDetail(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(
            @PathVariable Long id,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        loanService.deleteLoan(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<LoanPaymentResponse> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody LoanPaymentRequest request,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.recordPayment(id, request, user));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id,
            @PathVariable Long paymentId,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        loanService.deletePayment(id, paymentId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copy-payments")
    public ResponseEntity<List<LoanPaymentResponse>> copyPaymentsFromPreviousMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.copyPaymentsFromPreviousMonth(user, year, month));
    }
}
