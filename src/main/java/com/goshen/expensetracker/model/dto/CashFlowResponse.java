package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CashFlowResponse(
        int year,
        int month,
        List<CashFlowEvent> events,
        BigDecimal lowestBalance,
        LocalDate lowestBalanceDate,
        boolean willGoNegative
) {
    public record CashFlowEvent(
            LocalDate date,
            String description,
            String type,
            BigDecimal amount,
            BigDecimal runningBalance
    ) {}
}
