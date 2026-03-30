package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        String name,
        BigDecimal originalAmount,
        BigDecimal remainingBalance,
        BigDecimal totalPaid,
        LocalDateTime createdAt
) {}
