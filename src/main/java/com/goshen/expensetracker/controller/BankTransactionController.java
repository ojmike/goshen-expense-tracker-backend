package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.BankTransactionResponse;
import com.goshen.expensetracker.model.dto.TransactionCategoryUpdate;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.BankTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class BankTransactionController {

    private final BankTransactionService bankTransactionService;

    @GetMapping
    public ResponseEntity<List<BankTransactionResponse>> getTransactions(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bankTransactionService.getTransactions(user, year, month));
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<BankTransactionResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TransactionCategoryUpdate request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bankTransactionService.updateCategory(id, request.categoryId(), user));
    }

    @PutMapping("/{id}/reviewed")
    public ResponseEntity<BankTransactionResponse> markReviewed(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(bankTransactionService.markReviewed(id, user));
    }
}
