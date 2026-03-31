package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.BankTransactionResponse;
import com.goshen.expensetracker.model.entity.BankTransaction;
import com.goshen.expensetracker.model.entity.ExpenseCategory;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.BankTransactionRepository;
import com.goshen.expensetracker.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankTransactionService {

    private final BankTransactionRepository bankTransactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    @Transactional(readOnly = true)
    public List<BankTransactionResponse> getTransactions(User user, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        return bankTransactionRepository.findByUserIdAndMonth(user.getId(), start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BankTransactionResponse updateCategory(Long id, Long categoryId, User user) {
        BankTransaction txn = bankTransactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        ExpenseCategory category = expenseCategoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        txn.setCategory(category);
        txn.setReviewed(true);
        bankTransactionRepository.save(txn);

        return toResponse(txn);
    }

    public BankTransactionResponse markReviewed(Long id, User user) {
        BankTransaction txn = bankTransactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        txn.setReviewed(true);
        bankTransactionRepository.save(txn);

        return toResponse(txn);
    }

    private BankTransactionResponse toResponse(BankTransaction txn) {
        return new BankTransactionResponse(
                txn.getId(),
                txn.getName(),
                txn.getAmount(),
                txn.getTransactionDate(),
                txn.getPlaidCategory(),
                txn.getCategory() != null ? txn.getCategory().getId() : null,
                txn.getCategory() != null ? txn.getCategory().getName() : null,
                txn.isReviewed(),
                txn.getCreatedAt()
        );
    }
}
