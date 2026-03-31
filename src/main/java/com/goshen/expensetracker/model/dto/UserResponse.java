package com.goshen.expensetracker.model.dto;

public record UserResponse(Long id, String email, String firstName, String lastName,
                           Integer trackingStartYear, Integer trackingStartMonth) {}
