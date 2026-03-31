package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String name,
        BigDecimal amount,
        String expenseType,
        LocalDate expenseDate,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {}
