package com.goshen.expensetracker.model.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        boolean isDefault,
        LocalDateTime createdAt
) {}
