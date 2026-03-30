package com.goshen.expensetracker.model.dto;

import jakarta.validation.constraints.NotNull;

public record TransactionCategoryUpdate(
        @NotNull Long categoryId
) {}
