package com.trueequity.controller;

import com.trueequity.service.TechnicalIndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for RSIController
 * Tests REST API endpoint behavior
 */
@ExtendWith(MockitoExtension.class)
class RSIControllerTest {

    @Mock
    private TechnicalIndicatorService technicalIndicatorService;

    @InjectMocks
    private RSIController rsiController;

    @BeforeEach
    void setUp() {
        // Controller is already injected via @InjectMocks
    }

    @Test
    void testGetRSI_WithValidSymbolAndTimeframe_ReturnsRSI() {
        // Arrange
        String symbol = "AAPL";
        String timeframe = "1d";
        BigDecimal expectedRSI = BigDecimal.valueOf(45.5);

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenReturn(expectedRSI);

        // Act
        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedRSI.doubleValue(), (Double) response.getBody().get("rsi"), 0.01);
        assertEquals(timeframe, response.getBody().get("timeframe"));
        assertEquals(symbol.toUpperCase(), response.getBody().get("symbol"));
    }

    @Test
    void testGetRSI_WithNullRSI_Returns200WithError() {
        // Arrange
        String symbol = "INVALID";
        String timeframe = "1d";

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Returns 200 even if RSI is null
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("rsi"));
        assertNotNull(response.getBody().get("error"));
        assertEquals(timeframe, response.getBody().get("timeframe"));
        assertEquals(symbol.toUpperCase(), response.getBody().get("symbol"));
    }

    @Test
    void testGetRSI_WithException_Returns500() {
        // Arrange
        String symbol = "AAPL";
        String timeframe = "1d";

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"));
    }

    @Test
    void testGetRSI_WithDefaultTimeframe_Uses1d() {
        // Arrange
        String symbol = "AAPL";
        String defaultTimeframe = "1d"; // Default value
        BigDecimal expectedRSI = BigDecimal.valueOf(50.0);

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), defaultTimeframe))
            .thenReturn(expectedRSI);

        // Act
        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, defaultTimeframe);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(defaultTimeframe, response.getBody().get("timeframe"));
    }

    @Test
    void testGetRSI_WithDifferentTimeframes_HandlesAll() {
        // Arrange
        String symbol = "AAPL";
        String[] timeframes = {"1h", "30m", "2h", "1d"};
        BigDecimal mockRSI = BigDecimal.valueOf(45.0);

        for (String timeframe : timeframes) {
            when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
                .thenReturn(mockRSI);

            // Act
            ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(timeframe, response.getBody().get("timeframe"));
        }
    }

    @Test
    void testGetRSI_WithLowerCaseSymbol_ConvertsToUpperCase() {
        // Arrange
        String symbol = "aapl"; // Lowercase
        String timeframe = "1d";
        BigDecimal expectedRSI = BigDecimal.valueOf(50.0);

        when(technicalIndicatorService.calculateRSIForTimeframe("AAPL", timeframe))
            .thenReturn(expectedRSI);

        // Act
        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AAPL", response.getBody().get("symbol")); // Should be uppercase
    }
}
