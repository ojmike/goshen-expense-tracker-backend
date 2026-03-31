package com.goshen.expensetracker.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Locale;

@Configuration
public class PlaidConfig {

    @Value("${plaid.client-id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    @Value("${plaid.environment:sandbox}")
    private String environment;

    @Bean
    public PlaidApi plaidApi() {
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);

        ApiClient apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(resolveBaseUrl());

        return apiClient.createService(PlaidApi.class);
    }

    private String resolveBaseUrl() {
        return switch (environment.toLowerCase(Locale.ROOT)) {
            case "production" -> ApiClient.Production;
            case "development" -> ApiClient.Development;
            case "sandbox" -> ApiClient.Sandbox;
            default -> throw new IllegalArgumentException("Unsupported plaid.environment: " + environment);
        };
    }
}
