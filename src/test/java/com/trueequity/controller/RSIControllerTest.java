package com.trueequity.controller;

import com.trueequity.repository.JdbcTechnicalIndicatorRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for RSIController
 * Controller reads from repository first, then falls back to service.
 */
@ExtendWith(MockitoExtension.class)
class RSIControllerTest {

    @Mock
    private TechnicalIndicatorService technicalIndicatorService;

    @Mock
    private JdbcTechnicalIndicatorRepository technicalIndicatorRepository;

    @InjectMocks
    private RSIController rsiController;

    @BeforeEach
    void setUp() {
        // Repository returns null by default so controller falls back to service
        when(technicalIndicatorRepository.getLatestRSIForTimeframe(anyString(), anyString())).thenReturn(null);
    }

    @Test
    void testGetRSI_WithValidSymbolAndTimeframe_ReturnsRSI() {
        String symbol = "AAPL";
        String timeframe = "1d";
        BigDecimal expectedRSI = BigDecimal.valueOf(45.5);

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenReturn(expectedRSI);

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedRSI.doubleValue(), (Double) response.getBody().get("rsi"), 0.01);
        assertEquals(timeframe, response.getBody().get("timeframe"));
        assertEquals(symbol.toUpperCase(), response.getBody().get("symbol"));
    }

    @Test
    void testGetRSI_WithNullRSI_Returns200WithError() {
        String symbol = "INVALID";
        String timeframe = "1d";

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenReturn(null);

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("rsi"));
        assertNotNull(response.getBody().get("error"));
        assertEquals(timeframe, response.getBody().get("timeframe"));
        assertEquals(symbol.toUpperCase(), response.getBody().get("symbol"));
    }

    @Test
    void testGetRSI_WithException_Returns500() {
        String symbol = "AAPL";
        String timeframe = "1d";

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("error"));
    }

    @Test
    void testGetRSI_WithDefaultTimeframe_Uses1d() {
        String symbol = "AAPL";
        String defaultTimeframe = "1d";
        BigDecimal expectedRSI = BigDecimal.valueOf(50.0);

        when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), defaultTimeframe))
            .thenReturn(expectedRSI);

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, defaultTimeframe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(defaultTimeframe, response.getBody().get("timeframe"));
    }

    @Test
    void testGetRSI_WithDifferentTimeframes_HandlesAll() {
        String symbol = "AAPL";
        String[] timeframes = {"1h", "30m", "2h", "1d"};
        BigDecimal mockRSI = BigDecimal.valueOf(45.0);

        for (String timeframe : timeframes) {
            when(technicalIndicatorService.calculateRSIForTimeframe(symbol.toUpperCase(), timeframe))
                .thenReturn(mockRSI);

            ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(timeframe, response.getBody().get("timeframe"));
        }
    }

    @Test
    void testGetRSI_WithLowerCaseSymbol_ConvertsToUpperCase() {
        String symbol = "aapl";
        String timeframe = "1d";
        BigDecimal expectedRSI = BigDecimal.valueOf(50.0);

        when(technicalIndicatorService.calculateRSIForTimeframe("AAPL", timeframe))
            .thenReturn(expectedRSI);

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AAPL", response.getBody().get("symbol"));
    }

    @Test
    void testGetRSI_WhenRepositoryHasValue_ReturnsFromDb() {
        String symbol = "AAPL";
        String timeframe = "1d";
        BigDecimal storedRSI = BigDecimal.valueOf(55.0);

        when(technicalIndicatorRepository.getLatestRSIForTimeframe(symbol.toUpperCase(), timeframe))
            .thenReturn(storedRSI);

        ResponseEntity<Map<String, Object>> response = rsiController.getRSI(symbol, timeframe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(storedRSI.doubleValue(), (Double) response.getBody().get("rsi"), 0.01);
        verify(technicalIndicatorService, never()).calculateRSIForTimeframe(anyString(), anyString());
    }
}
