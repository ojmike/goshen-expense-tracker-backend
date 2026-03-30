package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanPaymentResponse(
        Long id,
        BigDecimal amount,
        LocalDate paymentDate,
        String note,
        BigDecimal balanceAfterPayment,
        LocalDateTime createdAt
) {}
