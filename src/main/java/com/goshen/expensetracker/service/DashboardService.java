package com.goshen.expensetracker.service;

import com.goshen.expensetracker.model.dto.DashboardResponse;
import com.goshen.expensetracker.model.dto.IncomeOverviewResponse;
import com.goshen.expensetracker.model.dto.LoanResponse;
import com.goshen.expensetracker.model.dto.MonthlyExpenseOverview;
import com.goshen.expensetracker.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal leftover = totalIncome.subtract(totalExpenses);

        BigDecimal totalRemainingDebt = loans.stream()
                .map(LoanResponse::remainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebtPaid = loans.stream()
                .map(LoanResponse::totalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(
                year,
                month,
                totalIncome,
                income.sourceCount(),
                totalExpenses,
                expenses.expenseCount(),
                leftover,
                totalRemainingDebt,
                totalDebtPaid,
                loans.size()
        );
    }
}
