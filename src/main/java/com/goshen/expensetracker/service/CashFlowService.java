package com.goshen.expensetracker.service;

import com.goshen.expensetracker.model.dto.CashFlowResponse;
import com.goshen.expensetracker.model.dto.CashFlowResponse.CashFlowEvent;
import com.goshen.expensetracker.model.entity.*;
import com.goshen.expensetracker.repository.ExpenseRepository;
import com.goshen.expensetracker.repository.IncomeSourceRepository;
import com.goshen.expensetracker.repository.LoanPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashFlowService {

    private final IncomeSourceRepository incomeSourceRepository;
    private final ExpenseRepository expenseRepository;
    private final LoanPaymentRepository loanPaymentRepository;

    public CashFlowResponse getCashFlow(User user, int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1);
        int daysInMonth = monthStart.lengthOfMonth();

        List<CashFlowEvent> events = new ArrayList<>();

        // Income events
        List<IncomeSource> sources = incomeSourceRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (IncomeSource source : sources) {
            List<LocalDate> payDates = getPayDatesForMonth(source, year, month, daysInMonth);
            for (LocalDate payDate : payDates) {
                events.add(new CashFlowEvent(
                        payDate,
                        source.getName(),
                        "INCOME",
                        source.getAmount(),
                        BigDecimal.ZERO
                ));
            }
        }

        // Expense events
        List<Expense> expenses = expenseRepository.findByUserIdAndMonth(user.getId(), monthStart, monthEnd);
        for (Expense expense : expenses) {
            String categoryName = expense.getCategory() != null
                    ? expense.getCategory().getName()
                    : "Uncategorized";
            events.add(new CashFlowEvent(
                    expense.getExpenseDate(),
                    expense.getName() + " (" + categoryName + ")",
                    "EXPENSE",
                    expense.getAmount().negate(),
                    BigDecimal.ZERO
            ));
        }

        // Loan payment events
        List<LoanPayment> payments = loanPaymentRepository.findByUserIdAndMonth(user.getId(), monthStart, monthEnd);
        for (LoanPayment payment : payments) {
            events.add(new CashFlowEvent(
                    payment.getPaymentDate(),
                    payment.getLoan().getName() + " payment",
                    "LOAN_PAYMENT",
                    payment.getAmount().negate(),
                    BigDecimal.ZERO
            ));
        }

        // Sort by date, income first on same day
        events.sort(Comparator.comparing(CashFlowEvent::date)
                .thenComparing(e -> e.type().equals("INCOME") ? 0 : 1));

        // Calculate running balance
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal lowestBalance = null;
        LocalDate lowestDate = monthStart;
        List<CashFlowEvent> withBalances = new ArrayList<>();

        for (CashFlowEvent event : events) {
            balance = balance.add(event.amount());
            withBalances.add(new CashFlowEvent(
                    event.date(),
                    event.description(),
                    event.type(),
                    event.amount(),
                    balance
            ));
            if (lowestBalance == null || balance.compareTo(lowestBalance) < 0) {
                lowestBalance = balance;
                lowestDate = event.date();
            }
        }

        if (lowestBalance == null) {
            lowestBalance = BigDecimal.ZERO;
        }

        return new CashFlowResponse(
                year,
                month,
                withBalances,
                lowestBalance,
                lowestDate,
                lowestBalance.compareTo(BigDecimal.ZERO) < 0
        );
    }

    private List<LocalDate> getPayDatesForMonth(IncomeSource source, int year, int month, int daysInMonth) {
        List<LocalDate> dates = new ArrayList<>();
        PayFrequency freq = source.getFrequency();

        switch (freq) {
            case MONTHLY -> {
                int day = Math.min(source.getNextPayDate().getDayOfMonth(), daysInMonth);
                dates.add(LocalDate.of(year, month, day));
            }
            case BIWEEKLY -> {
                int firstDay = Math.min(source.getNextPayDate().getDayOfMonth(), daysInMonth);
                dates.add(LocalDate.of(year, month, firstDay));
                int secondDay;
                if (source.getSecondPayDay() != null) {
                    secondDay = Math.min(source.getSecondPayDay(), daysInMonth);
                } else {
                    // Default: offset by ~15 days
                    secondDay = firstDay <= 15 ? Math.min(firstDay + 15, daysInMonth) : Math.max(firstDay - 15, 1);
                }
                dates.add(LocalDate.of(year, month, secondDay));
            }
            case WEEKLY -> {
                // Find all occurrences of the pay weekday in this month
                LocalDate refDate = source.getNextPayDate();
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.plusMonths(1);
                // Find first occurrence of this weekday in the month
                LocalDate current = monthStart;
                while (current.getDayOfWeek() != refDate.getDayOfWeek()) {
                    current = current.plusDays(1);
                }
                while (current.isBefore(monthEnd)) {
                    dates.add(current);
                    current = current.plusWeeks(1);
                }
            }
        }
        return dates;
    }
}
