package com.goshen.expensetracker.model.dto;

import com.goshen.expensetracker.model.entity.PayFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeSourceRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "Frequency is required")
        PayFrequency frequency,

        @NotNull(message = "Next pay date is required")
        LocalDate nextPayDate
) {}
