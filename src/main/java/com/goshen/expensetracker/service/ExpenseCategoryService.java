package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.CategoryRequest;
import com.goshen.expensetracker.model.dto.CategoryResponse;
import com.goshen.expensetracker.model.entity.ExpenseCategory;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.BankTransactionRepository;
import com.goshen.expensetracker.repository.ExpenseCategoryRepository;
import com.goshen.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseCategoryService {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Rent", "Car Insurance", "Car Payment", "Electricity",
            "School Loan", "Credit Card Loan", "Tithe", "Food", "Gas", "Savings"
    );

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public void seedDefaultCategories(User user) {
        for (String name : DEFAULT_CATEGORIES) {
            // Make seeding idempotent — skip if already exists
            if (categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), name)) {
                continue;
            }
            ExpenseCategory category = new ExpenseCategory();
            category.setUser(user);
            category.setName(name);
            category.setDefault(true);
            categoryRepository.save(category);
        }
    }

    public List<CategoryResponse> getAll(User user) {
        List<ExpenseCategory> categories = categoryRepository.findByUserIdOrderByNameAsc(user.getId());
        if (categories.isEmpty()) {
            seedDefaultCategories(user);
            categories = categoryRepository.findByUserIdOrderByNameAsc(user.getId());
        }
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse create(CategoryRequest request, User user) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.name())) {
            throw new IllegalArgumentException("A category with this name already exists");
        }

        ExpenseCategory category = new ExpenseCategory();
        category.setUser(user);
        category.setName(request.name());
        category.setDefault(false);

        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request, User user) {
        ExpenseCategory category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Allow keeping the same name (case-insensitive), reject if another category has this name
        boolean nameUnchanged = category.getName().equalsIgnoreCase(request.name());
        if (!nameUnchanged && categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.name())) {
            throw new IllegalArgumentException("A category with this name already exists");
        }

        category.setName(request.name());
        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long id, User user) {
        ExpenseCategory category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (expenseRepository.existsByCategoryId(category.getId())) {
            throw new IllegalArgumentException("Cannot delete a category that has expenses attached");
        }

        if (bankTransactionRepository.existsByCategoryId(category.getId())) {
            throw new IllegalArgumentException("Cannot delete a category that has bank transactions attached");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(ExpenseCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isDefault(),
                category.getCreatedAt()
        );
    }
}
