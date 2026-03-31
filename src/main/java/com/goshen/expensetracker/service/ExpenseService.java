package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.ExpenseRequest;
import com.goshen.expensetracker.model.dto.ExpenseResponse;
import com.goshen.expensetracker.model.dto.MonthlyExpenseOverview;
import com.goshen.expensetracker.model.entity.Expense;
import com.goshen.expensetracker.model.entity.ExpenseCategory;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.ExpenseCategoryRepository;
import com.goshen.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public MonthlyExpenseOverview getMonthlyOverview(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        List<Expense> expenses = expenseRepository.findByUserIdAndMonth(user.getId(), startDate, endDate);
        List<ExpenseResponse> responses = expenses.stream()
                .map(this::toResponse)
                .toList();

        BigDecimal total = responses.stream()
                .map(ExpenseResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyExpenseOverview(year, month, total, responses.size(), responses);
    }

    public ExpenseResponse create(ExpenseRequest request, User user) {
        ExpenseCategory category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setName(request.name());
        expense.setAmount(request.amount());
        expense.setExpenseType(request.expenseType());
        expense.setExpenseDate(request.expenseDate());

        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse update(Long id, ExpenseRequest request, User user) {
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        ExpenseCategory category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        expense.setCategory(category);
        expense.setName(request.name());
        expense.setAmount(request.amount());
        expense.setExpenseType(request.expenseType());
        expense.setExpenseDate(request.expenseDate());

        return toResponse(expenseRepository.save(expense));
    }

    public void delete(Long id, User user) {
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public List<ExpenseResponse> copyFromPreviousMonth(User user, int year, int month) {
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        LocalDate prevStart = targetMonth.minusMonths(1);
        LocalDate prevEnd = targetMonth;

        List<Expense> previousExpenses = expenseRepository.findByUserIdAndMonth(user.getId(), prevStart, prevEnd);

        if (previousExpenses.isEmpty()) {
            throw new ResourceNotFoundException("No expenses found in the previous month to copy");
        }

        List<Expense> copies = previousExpenses.stream().map(prev -> {
            Expense copy = new Expense();
            copy.setUser(user);
            copy.setCategory(prev.getCategory());
            copy.setName(prev.getName());
            copy.setAmount(prev.getAmount());
            copy.setExpenseType(prev.getExpenseType());
            int day = Math.min(prev.getExpenseDate().getDayOfMonth(), targetMonth.lengthOfMonth());
            copy.setExpenseDate(targetMonth.withDayOfMonth(day));
            return copy;
        }).toList();

        return expenseRepository.saveAll(copies).stream()
                .map(this::toResponse)
                .toList();
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getAmount(),
                expense.getExpenseType().name(),
                expense.getExpenseDate(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getCreatedAt()
        );
    }
}
