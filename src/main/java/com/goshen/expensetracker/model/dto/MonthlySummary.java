package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;

public record MonthlySummary(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal leftover,
        BigDecimal savingsAmount,
        BigDecimal savingsRate
) {}
