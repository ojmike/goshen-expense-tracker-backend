package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncomeSourceResponse(
        Long id,
        String name,
        BigDecimal amount,
        String frequency,
        LocalDate nextPayDate,
        Integer secondPayDay,
        BigDecimal monthlyEquivalent,
        LocalDateTime createdAt
) {}
