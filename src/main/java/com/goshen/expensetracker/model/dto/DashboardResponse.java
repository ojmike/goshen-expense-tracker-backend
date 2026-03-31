package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        int year,
        int month,
        BigDecimal totalMonthlyIncome,
        int incomeSourceCount,
        List<IncomeSourceResponse> incomeSources,
        BigDecimal totalMonthlyExpenses,
        int expenseCount,
        List<CategoryBreakdown> expensesByCategory,
        BigDecimal carryOver,
        BigDecimal leftover,
        BigDecimal totalRemainingDebt,
        BigDecimal totalDebtPaid,
        int loanCount,
        List<LoanResponse> loans
) {
    public record CategoryBreakdown(
            String categoryName,
            BigDecimal totalAmount,
            int count
    ) {}
}
