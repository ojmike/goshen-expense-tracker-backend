package com.goshen.expensetracker.service;

import com.goshen.expensetracker.model.dto.DashboardResponse;
import com.goshen.expensetracker.model.dto.ExpenseResponse;
import com.goshen.expensetracker.model.dto.IncomeOverviewResponse;
import com.goshen.expensetracker.model.dto.LoanResponse;
import com.goshen.expensetracker.model.dto.MonthlyExpenseOverview;
import com.goshen.expensetracker.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final IncomeSourceService incomeSourceService;
    private final ExpenseService expenseService;
    private final LoanService loanService;

    public DashboardResponse getOverview(User user, int year, int month) {
        IncomeOverviewResponse income = incomeSourceService.getOverview(user);
        MonthlyExpenseOverview expenses = expenseService.getMonthlyOverview(user, year, month);
        List<LoanResponse> loans = loanService.getAllLoans(user);

        BigDecimal totalIncome = income.totalMonthlyIncome();
        BigDecimal totalExpenses = expenses.totalAmount();

        // Carry over: chain forward from the user's tracking start month
        // Each month: leftover = income + carry - expenses → becomes next month's carry
        java.time.LocalDate target = java.time.LocalDate.of(year, month, 1);
        BigDecimal carryOver = BigDecimal.ZERO;
        if (user.getTrackingStartYear() != null && user.getTrackingStartMonth() != null) {
            java.time.LocalDate cursor = java.time.LocalDate.of(
                    user.getTrackingStartYear(), user.getTrackingStartMonth(), 1);
            while (cursor.isBefore(target)) {
                MonthlyExpenseOverview mo = expenseService.getMonthlyOverview(user, cursor.getYear(), cursor.getMonthValue());
                carryOver = totalIncome.add(carryOver).subtract(mo.totalAmount());
                cursor = cursor.plusMonths(1);
            }
        }

        BigDecimal leftover = totalIncome.add(carryOver).subtract(totalExpenses);

        BigDecimal totalRemainingDebt = loans.stream()
                .map(LoanResponse::remainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebtPaid = loans.stream()
                .map(LoanResponse::totalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardResponse.CategoryBreakdown> expensesByCategory = expenses.expenses().stream()
                .collect(Collectors.groupingBy(
                        ExpenseResponse::categoryName,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> new DashboardResponse.CategoryBreakdown(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(ExpenseResponse::amount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        entry.getValue().size()
                ))
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .toList();

        return new DashboardResponse(
                year,
                month,
                totalIncome,
                income.sourceCount(),
                income.sources(),
                totalExpenses,
                expenses.expenseCount(),
                expensesByCategory,
                carryOver,
                leftover,
                totalRemainingDebt,
                totalDebtPaid,
                loans.size(),
                loans
        );
    }
}
