package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyExpenseOverview(
        int year,
        int month,
        BigDecimal totalAmount,
        int expenseCount,
        List<ExpenseResponse> expenses
) {}
