package com.goshen.expensetracker.controller;

import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        String dbStatus = checkDatabase();
        return Map.of(
                "status", "UP",
                "database", dbStatus
        );
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "CONNECTED" : "DISCONNECTED";
        } catch (Exception e) {
            return "DISCONNECTED";
        }
    }
}
