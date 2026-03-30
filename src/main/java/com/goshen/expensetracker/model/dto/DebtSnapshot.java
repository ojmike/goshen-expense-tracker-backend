package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;

public record DebtSnapshot(
        int year,
        int month,
        BigDecimal totalRemainingDebt,
        BigDecimal totalPaid
) {}
