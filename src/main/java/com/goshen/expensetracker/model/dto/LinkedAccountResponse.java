package com.goshen.expensetracker.model.dto;

import java.time.LocalDateTime;

public record LinkedAccountResponse(
        Long id,
        String institutionName,
        String accountName,
        String accountMask,
        LocalDateTime createdAt
) {}
