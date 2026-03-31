package com.goshen.expensetracker.controller;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        String dbStatus = checkDatabase();
        boolean isUp = "CONNECTED".equals(dbStatus);
        Map<String, String> body = Map.of(
                "status", isUp ? "UP" : "DOWN",
                "database", dbStatus
        );
        return ResponseEntity.status(isUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "CONNECTED" : "DISCONNECTED";
        } catch (Exception e) {
            logger.warn("Database health check failed: {}", e.getMessage(), e);
            return "DISCONNECTED";
        }
    }
}
