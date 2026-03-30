package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        int year,
        int month,
        BigDecimal totalMonthlyIncome,
        int incomeSourceCount,
        BigDecimal totalMonthlyExpenses,
        int expenseCount,
        BigDecimal leftover,
        BigDecimal totalRemainingDebt,
        BigDecimal totalDebtPaid,
        int loanCount
) {}
