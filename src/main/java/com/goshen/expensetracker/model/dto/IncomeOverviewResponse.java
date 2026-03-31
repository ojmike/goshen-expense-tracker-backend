package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record IncomeOverviewResponse(
        BigDecimal totalMonthlyIncome,
        int sourceCount,
        List<IncomeSourceResponse> sources
) {}
