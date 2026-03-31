package com.goshen.expensetracker.service;

import com.goshen.expensetracker.model.dto.*;
import com.goshen.expensetracker.model.entity.Expense;
import com.goshen.expensetracker.model.entity.Loan;
import com.goshen.expensetracker.model.entity.LoanPayment;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.ExpenseRepository;
import com.goshen.expensetracker.repository.LoanPaymentRepository;
import com.goshen.expensetracker.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final IncomeSourceService incomeSourceService;
    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;

    public AnalyticsResponse getAnalytics(User user, int year, int month, int months) {
        List<CategoryTrend> categoryTrends = buildCategoryTrends(user, year, month);
        List<MonthlySummary> monthlySummaries = buildMonthlySummaries(user, year, month, months);
        List<DebtSnapshot> debtSnapshots = buildDebtSnapshots(user);
        return new AnalyticsResponse(categoryTrends, monthlySummaries, debtSnapshots);
    }

    private List<CategoryTrend> buildCategoryTrends(User user, int year, int month) {
        YearMonth current = YearMonth.of(year, month);
        YearMonth previous = current.minusMonths(1);

        Map<String, BigDecimal> currentTotals = getExpensesByCategory(user, current);
        Map<String, BigDecimal> previousTotals = getExpensesByCategory(user, previous);

        Set<String> allCategories = new TreeSet<>();
        allCategories.addAll(currentTotals.keySet());
        allCategories.addAll(previousTotals.keySet());

        return allCategories.stream().map(category -> {
            BigDecimal curr = currentTotals.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal prev = previousTotals.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal changePercent;
            if (prev.compareTo(BigDecimal.ZERO) == 0) {
                changePercent = curr.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : new BigDecimal("100.00");
            } else {
                changePercent = curr.subtract(prev)
                        .multiply(new BigDecimal("100"))
                        .divide(prev, 2, RoundingMode.HALF_UP);
            }
            return new CategoryTrend(category, curr, prev, changePercent);
        }).toList();
    }

    private Map<String, BigDecimal> getExpensesByCategory(User user, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        List<Expense> expenses = expenseRepository.findByUserIdAndMonth(user.getId(), start, end);
        return expenses.stream().collect(
                Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                )
        );
    }

    private List<MonthlySummary> buildMonthlySummaries(User user, int year, int month, int months) {
        BigDecimal totalIncome = incomeSourceService.getOverview(user).totalMonthlyIncome();

        List<MonthlySummary> summaries = new ArrayList<>();
        YearMonth current = YearMonth.of(year, month);

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.plusMonths(1).atDay(1);

            List<Expense> expenses = expenseRepository.findByUserIdAndMonth(user.getId(), start, end);

            BigDecimal totalExpenses = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal savingsAmount = expenses.stream()
                    .filter(e -> e.getCategory().getName().equalsIgnoreCase("Savings"))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal leftover = totalIncome.subtract(totalExpenses);

            BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : savingsAmount.multiply(new BigDecimal("100"))
                            .divide(totalIncome, 2, RoundingMode.HALF_UP);

            summaries.add(new MonthlySummary(
                    ym.getYear(), ym.getMonthValue(),
                    totalIncome, totalExpenses, leftover,
                    savingsAmount, savingsRate
            ));
        }
        return summaries;
    }

    private List<DebtSnapshot> buildDebtSnapshots(User user) {
        List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (loans.isEmpty()) {
            return List.of();
        }

        BigDecimal totalOriginal = loans.stream()
                .map(Loan::getOriginalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Collect all payments across all loans, grouped by month
        Map<YearMonth, BigDecimal> paymentsByMonth = new TreeMap<>();
        for (Loan loan : loans) {
            List<LoanPayment> payments = loanPaymentRepository
                    .findByLoanIdOrderByPaymentDateAscCreatedAtAsc(loan.getId());
            for (LoanPayment payment : payments) {
                YearMonth ym = YearMonth.from(payment.getPaymentDate());
                paymentsByMonth.merge(ym, payment.getAmount(), BigDecimal::add);
            }
        }

        if (paymentsByMonth.isEmpty()) {
            return List.of();
        }

        // Build cumulative debt snapshots
        List<DebtSnapshot> snapshots = new ArrayList<>();
        BigDecimal cumulativePaid = BigDecimal.ZERO;

        for (Map.Entry<YearMonth, BigDecimal> entry : paymentsByMonth.entrySet()) {
            cumulativePaid = cumulativePaid.add(entry.getValue());
            BigDecimal remaining = totalOriginal.subtract(cumulativePaid).max(BigDecimal.ZERO);
            snapshots.add(new DebtSnapshot(
                    entry.getKey().getYear(),
                    entry.getKey().getMonthValue(),
                    remaining,
                    cumulativePaid
            ));
        }
        return snapshots;
    }
}
