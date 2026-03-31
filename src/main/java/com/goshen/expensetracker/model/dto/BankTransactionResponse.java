package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BankTransactionResponse(
        Long id,
        String name,
        BigDecimal amount,
        LocalDate transactionDate,
        String plaidCategory,
        Long categoryId,
        String categoryName,
        boolean reviewed,
        LocalDateTime createdAt
) {}
