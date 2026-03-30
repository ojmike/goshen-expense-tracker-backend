package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.ExchangeTokenRequest;
import com.goshen.expensetracker.model.dto.LinkTokenResponse;
import com.goshen.expensetracker.model.dto.LinkedAccountResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.PlaidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {

    private final PlaidService plaidService;

    @PostMapping("/link-token")
    public ResponseEntity<LinkTokenResponse> createLinkToken(Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        String linkToken = plaidService.createLinkToken(user);
        return ResponseEntity.ok(new LinkTokenResponse(linkToken));
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<LinkedAccountResponse> exchangeToken(
            @Valid @RequestBody ExchangeTokenRequest request,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        LinkedAccountResponse response = plaidService.exchangeToken(
                request.publicToken(), request.institutionName(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<LinkedAccountResponse>> getAccounts(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(plaidService.getLinkedAccounts(user));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> unlinkAccount(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        plaidService.unlinkAccount(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/{id}/sync")
    public ResponseEntity<Map<String, Integer>> syncTransactions(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        int synced = plaidService.syncTransactions(id, user);
        return ResponseEntity.ok(Map.of("synced", synced));
    }
}
