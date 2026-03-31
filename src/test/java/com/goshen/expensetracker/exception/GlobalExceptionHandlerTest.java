package com.goshen.expensetracker.exception;

import com.goshen.expensetracker.model.dto.ErrorResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneralReturnsInternalServerError() {
        var response = handler.handleGeneral(new RuntimeException("test"));
        ErrorResponse body = response.getBody();

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal server error", body.error());
        assertEquals(500, body.status());
    }
}
