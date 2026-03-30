package com.goshen.expensetracker.model.dto;

import java.math.BigDecimal;

public record CategoryTrend(
        String categoryName,
        BigDecimal currentMonthAmount,
        BigDecimal previousMonthAmount,
        BigDecimal changePercent
) {}
