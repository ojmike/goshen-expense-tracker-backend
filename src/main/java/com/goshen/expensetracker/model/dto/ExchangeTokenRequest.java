package com.goshen.expensetracker.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeTokenRequest(
        @NotBlank String publicToken,
        String institutionName
) {}
