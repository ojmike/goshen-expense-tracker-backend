package com.goshen.expensetracker.model.dto;

import java.util.List;

public record AnalyticsResponse(
        List<CategoryTrend> categoryTrends,
        List<MonthlySummary> monthlySummaries,
        List<DebtSnapshot> debtSnapshots
) {}
