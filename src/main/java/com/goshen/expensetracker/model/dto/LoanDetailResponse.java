package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LoanDetailResponse(
        Long id,
        String name,
        BigDecimal originalAmount,
        BigDecimal remainingBalance,
        BigDecimal totalPaid,
        LocalDateTime createdAt,
        List<LoanPaymentResponse> payments
) {}
