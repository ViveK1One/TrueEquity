package com.trueequity.controller;

import com.trueequity.repository.JdbcTechnicalIndicatorRepository;
import com.trueequity.service.TechnicalIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for RSI (Relative Strength Index) calculations by timeframe.
 * 
 * <p>This controller provides a REST API endpoint for calculating RSI values
 * for different timeframes (1h, 30m, 2h, 1d) to match TradingView's RSI calculations.
 * 
 * <p>Endpoint: GET /api/rsi/{symbol}?timeframe={timeframe}
 * 
 * <p>Example requests:
 * <ul>
 *   <li>GET /api/rsi/AAPL?timeframe=1h - Get 1-hour RSI for Apple</li>
 *   <li>GET /api/rsi/MSFT?timeframe=1d - Get daily RSI for Microsoft</li>
 * </ul>
 * 
 * <p>Response format:
 * <pre>
 * {
 *   "rsi": 45.5,
 *   "timeframe": "1d",
 *   "symbol": "AAPL"
 * }
 * </pre>
 * 
 * <p>If RSI cannot be calculated (insufficient data, API error), the endpoint returns
 * HTTP 200 with rsi: null and an error message, allowing the frontend to handle it gracefully.
 * 
 * @author TrueEquity Development Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/rsi")
@CrossOrigin(origins = "*")
public class RSIController {

    private final TechnicalIndicatorService technicalIndicatorService;
    private final JdbcTechnicalIndicatorRepository technicalIndicatorRepository;

    @Autowired
    public RSIController(TechnicalIndicatorService technicalIndicatorService,
                         JdbcTechnicalIndicatorRepository technicalIndicatorRepository) {
        this.technicalIndicatorService = technicalIndicatorService;
        this.technicalIndicatorRepository = technicalIndicatorRepository;
    }

    /**
     * Calculates and returns RSI for a given stock symbol and timeframe.
     * 
     * <p>This endpoint is called by the Next.js frontend to display RSI values
     * for different timeframes on the stock detail page.
     * 
     * @param symbol Stock symbol (e.g., "AAPL", "MSFT") - will be converted to uppercase
     * @param timeframe Timeframe: "1h", "30m", "2h", or "1d" (defaults to "1d" if not provided)
     * @return ResponseEntity with JSON containing:
     *         <ul>
     *           <li>rsi: RSI value (0-100) or null if calculation fails</li>
     *           <li>timeframe: The timeframe used</li>
     *           <li>symbol: The stock symbol (uppercase)</li>
     *           <li>error: Error message (only present if rsi is null)</li>
     *         </ul>
     *         Returns HTTP 200 even if RSI is null (with error message) to allow graceful frontend handling.
     *         Returns HTTP 500 only for unexpected server errors.
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getRSI(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {
        
        Map<String, Object> response = new HashMap<>();
        String sym = symbol.toUpperCase();
        String tf = (timeframe != null && !timeframe.isEmpty()) ? timeframe : "1d";

        try {
            // 1) Read from DB first (pre-computed for 1h, 30m, 2h, 1d)
            BigDecimal rsi = technicalIndicatorRepository.getLatestRSIForTimeframe(sym, tf);
            if (rsi != null) {
                response.put("rsi", rsi.doubleValue());
                response.put("timeframe", tf);
                response.put("symbol", sym);
                return ResponseEntity.ok(response);
            }
            // 2) Fallback: compute on-demand and store for next time
            rsi = technicalIndicatorService.calculateRSIForTimeframe(sym, tf);
            if (rsi != null) {
                technicalIndicatorRepository.upsertRSI(sym, LocalDate.now(), tf, rsi);
                response.put("rsi", rsi.doubleValue());
                response.put("timeframe", tf);
                response.put("symbol", sym);
                return ResponseEntity.ok(response);
            }
            response.put("error", "Could not get or calculate RSI for " + symbol + " with timeframe " + tf + ". This may be due to insufficient data or market being closed.");
            response.put("rsi", null);
            response.put("timeframe", tf);
            response.put("symbol", sym);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
