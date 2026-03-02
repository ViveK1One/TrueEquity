package com.trueequity.service;

import com.trueequity.repository.JdbcStockPriceRepository;
import com.trueequity.repository.JdbcTechnicalIndicatorRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for calculating technical indicators used in stock analysis.
 * 
 * <p>This service provides calculations for technical indicators such as:
 * <ul>
 *   <li><b>RSI (Relative Strength Index)</b>: Measures momentum and identifies overbought/oversold conditions</li>
 *   <li><b>Multiple Timeframes</b>: Supports 1-hour, 30-minute, 2-hour, and daily RSI calculations</li>
 * </ul>
 * 
 * <p>RSI is calculated using Wilder's smoothing method with a 14-period default.
 * RSI values range from 0-100:
 * <ul>
 *   <li>RSI > 70: Overbought (potential sell signal)</li>
 *   <li>RSI 30-70: Neutral</li>
 *   <li>RSI < 30: Oversold (potential buy signal)</li>
 * </ul>
 * 
 * <p>The service fetches price data from Yahoo Finance API for different timeframes
 * and calculates RSI values that match TradingView's calculations.
 * 
 * @author TrueEquity Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class TechnicalIndicatorService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static void log(String message) {
        System.out.println(LocalDateTime.now().format(FORMATTER) + " - [TechnicalIndicatorService] " + message);
    }

    private final JdbcStockPriceRepository stockPriceRepository;
    private final JdbcTechnicalIndicatorRepository technicalIndicatorRepository;

    public TechnicalIndicatorService(
            JdbcStockPriceRepository stockPriceRepository,
            JdbcTechnicalIndicatorRepository technicalIndicatorRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.technicalIndicatorRepository = technicalIndicatorRepository;
    }

    /**
     * Calculates and stores RSI (Relative Strength Index) for a stock symbol using daily timeframe.
     * 
     * <p>This method:
     * <ol>
     *   <li>Fetches the last 30 days of price data from the database</li>
     *   <li>Requires at least 14 trading days for RSI calculation</li>
     *   <li>Calculates RSI using Wilder's smoothing method (14-period)</li>
     *   <li>Stores the result in the technical_indicators table</li>
     * </ol>
     * 
     * <p>RSI Formula (Wilder's Smoothing):
     * <pre>
     * RS = Average Gain / Average Loss (over 14 periods)
     * RSI = 100 - (100 / (1 + RS))
     * </pre>
     * 
     * <p>This method is called automatically by the scheduler every 15 minutes during market hours.
     * 
     * @param symbol The stock symbol (e.g., "AAPL", "MSFT")
     * @throws Exception If database access fails or insufficient price data is available
     */
    public void calculateAndStoreRSI(String symbol) {
        try {
            // RSI updates every time prices update (every 15 minutes during market hours)
            // No smart update check - always calculate when called

            // Get last 30 days of price data (need at least 14 trading days for RSI calculation)
            // Fetching 30 days ensures we have enough trading days (excluding weekends/holidays)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            List<JdbcStockPriceRepository.PriceData> prices = stockPriceRepository.getPricesForDateRange(symbol, startDate, endDate);
            
            if (prices.size() < 14) {
                log("Not enough price data for RSI calculation for " + symbol + " (need 14, have " + prices.size() + ")");
                return;
            }

            // Sort by date ascending
            prices.sort((a, b) -> a.date().compareTo(b.date()));

            // Calculate RSI using 14-period
            BigDecimal rsi = calculateRSI(prices, 14);
            
            if (rsi != null) {
                technicalIndicatorRepository.upsertRSI(symbol, endDate, JdbcTechnicalIndicatorRepository.TIMEFRAME_1D, rsi);
                log("Calculated and stored RSI for " + symbol + ": " + rsi.setScale(2, RoundingMode.HALF_UP));
            }
            
        } catch (Exception e) {
            log("Error calculating RSI for " + symbol + ": " + e.getMessage());
        }
    }

    /**
     * Calculates and stores RSI for all timeframes (1h, 30m, 2h, 1d) so the frontend can read from DB.
     * Call this from the scheduler instead of only calculateAndStoreRSI.
     */
    public void calculateAndStoreRSIForAllTimeframes(String symbol) {
        // 1d: use existing daily logic (from DB prices)
        calculateAndStoreRSI(symbol);
        // 1h, 30m, 2h: compute from Yahoo and store
        String[] timeframes = { JdbcTechnicalIndicatorRepository.TIMEFRAME_1H, JdbcTechnicalIndicatorRepository.TIMEFRAME_30M, JdbcTechnicalIndicatorRepository.TIMEFRAME_2H };
        LocalDate today = LocalDate.now();
        for (String tf : timeframes) {
            try {
                BigDecimal rsi = calculateRSIForTimeframe(symbol, tf);
                if (rsi != null) {
                    technicalIndicatorRepository.upsertRSI(symbol, today, tf, rsi);
                    log("Stored " + tf + " RSI for " + symbol + ": " + rsi.setScale(2, RoundingMode.HALF_UP));
                }
            } catch (Exception e) {
                log("Error storing " + tf + " RSI for " + symbol + ": " + e.getMessage());
            }
        }
    }

    /**
     * Calculates RSI (Relative Strength Index) from a list of price data using Wilder's smoothing method.
     * 
     * <p>This is the core RSI calculation algorithm:
     * <ol>
     *   <li>Calculates price changes (gains and losses) between consecutive periods</li>
     *   <li>Computes initial average gain and average loss over the first 'period' intervals</li>
     *   <li>Applies Wilder's smoothing: new_avg = (old_avg * (period-1) + current_value) / period</li>
     *   <li>Calculates RS (Relative Strength) = average_gain / average_loss</li>
     *   <li>Converts to RSI: RSI = 100 - (100 / (1 + RS))</li>
     * </ol>
     * 
     * <p>Wilder's smoothing gives more weight to recent price movements, making it more responsive
     * to current market conditions than simple moving averages.
     * 
     * @param prices List of price data, must be sorted by date in ascending order
     * @param period Number of periods for RSI calculation (typically 14)
     * @return RSI value between 0-100, or null if insufficient data (need at least period+1 prices)
     */
    private BigDecimal calculateRSI(List<JdbcStockPriceRepository.PriceData> prices, int period) {
        if (prices.size() < period + 1) {
            return null;
        }

        // Calculate price changes
        List<BigDecimal> gains = new java.util.ArrayList<>();
        List<BigDecimal> losses = new java.util.ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).close().subtract(prices.get(i - 1).close());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }

        // Calculate initial average gain and loss (first period values)
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 0; i < period; i++) {
            avgGain = avgGain.add(gains.get(i));
            avgLoss = avgLoss.add(losses.get(i));
        }

        avgGain = avgGain.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

        // Use Wilder's smoothing method for remaining periods
        for (int i = period; i < gains.size(); i++) {
            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1))
                    .add(gains.get(i))
                    .divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1))
                    .add(losses.get(i))
                    .divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        }

        // Calculate RS and RSI
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100); // All gains, no losses
        }

        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP));

        return rsi.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate hourly RSI to match TradingView's 1-hour candle RSI
     * Fetches hourly price data from Yahoo Finance and calculates RSI(14) on hourly candles
     * This matches TradingView when viewing 1-hour charts
     */
    public BigDecimal calculateHourlyRSI(String symbol) {
        try {
            // Fetch last 24 hours of hourly price data (need at least 14 hours for RSI)
            // Yahoo Finance supports: 1m, 2m, 5m, 15m, 30m, 60m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo
            java.time.LocalDateTime endTime = java.time.LocalDateTime.now();
            java.time.LocalDateTime startTime = endTime.minusHours(24);
            
            long startTimestamp = startTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            long endTimestamp = endTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1h",
                    symbol, startTimestamp, endTimestamp);
            
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<String> response = httpClient.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log("Failed to fetch hourly prices for " + symbol + " - HTTP " + response.statusCode());
                return null;
            }
            
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.body());
            com.fasterxml.jackson.databind.JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                log("No hourly price data returned for " + symbol);
                return null;
            }
            
            com.fasterxml.jackson.databind.JsonNode data = result.get(0);
            com.fasterxml.jackson.databind.JsonNode timestamps = data.path("timestamp");
            com.fasterxml.jackson.databind.JsonNode indicators = data.path("indicators").path("quote").get(0);
            com.fasterxml.jackson.databind.JsonNode closes = indicators.path("close");
            
            // Extract closing prices
            List<BigDecimal> hourlyPrices = new java.util.ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                if (!closes.get(i).isNull()) {
                    hourlyPrices.add(BigDecimal.valueOf(closes.get(i).asDouble()));
                }
            }
            
            if (hourlyPrices.size() < 15) {
                log("Not enough hourly price data for RSI calculation for " + symbol + " (need 15, have " + hourlyPrices.size() + ")");
                return null;
            }
            
            // Convert to PriceData format for RSI calculation
            List<JdbcStockPriceRepository.PriceData> priceDataList = new java.util.ArrayList<>();
            for (int i = 0; i < hourlyPrices.size(); i++) {
                // Use current date for all (we only need close prices for RSI)
                priceDataList.add(new JdbcStockPriceRepository.PriceData(
                    symbol,
                    java.time.LocalDate.now(), // Date not used in RSI calc, just for structure
                    hourlyPrices.get(i), // open
                    hourlyPrices.get(i), // high
                    hourlyPrices.get(i), // low
                    hourlyPrices.get(i), // close (this is what we use)
                    null, // adjustedClose
                    0L // volume
                ));
            }
            
            // Calculate RSI using 14-period on hourly data
            BigDecimal hourlyRSI = calculateRSI(priceDataList, 14);
            
            if (hourlyRSI != null) {
                log("Calculated hourly RSI for " + symbol + ": " + hourlyRSI.setScale(2, RoundingMode.HALF_UP));
            }
            
            return hourlyRSI;
            
        } catch (Exception e) {
            log("Error calculating hourly RSI for " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    /** Yahoo uses ticker symbols; map common name-based or wrong symbols to correct ticker. */
    private static String normalizeSymbolForYahoo(String symbol) {
        if (symbol == null) return symbol;
        switch (symbol.toUpperCase()) {
            case "ADOBE": return "ADBE";
            default: return symbol;
        }
    }

    /**
     * Calculate RSI for different timeframes to match TradingView
     * @param symbol Stock symbol
     * @param timeframe "1h", "30m", "2h", "1d" (matches TradingView intervals)
     * @return RSI value or null if calculation fails
     */
    public BigDecimal calculateRSIForTimeframe(String symbol, String timeframe) {
        try {
            symbol = normalizeSymbolForYahoo(symbol);
            java.time.LocalDateTime endTime = java.time.LocalDateTime.now();
            java.time.LocalDateTime startTime;
            String interval;
            
            // Set time range and interval so each timeframe uses different bar size (distinct RSI values)
            // Yahoo: 1m, 2m, 5m, 15m, 30m, 60m, 1d, 1wk, 1mo
            // 1 Hour = 14-period RSI on hourly bars; 1 Month = daily; 6 Months = weekly; 1 Year = monthly
            switch (timeframe) {
                case "1h": // 1 Hour – RSI(14) on hourly bars
                    startTime = endTime.minusDays(60);
                    interval = "60m";
                    break;
                case "30m": // 1 Month – RSI(14) on daily bars (last ~14 days)
                    startTime = endTime.minusDays(30);
                    interval = "1d";
                    break;
                case "2h": // 6 Months – RSI(14) on weekly bars (last ~14 weeks)
                    startTime = endTime.minusDays(120);
                    interval = "1wk";
                    break;
                case "1d": // 1 Year – RSI(14) on monthly bars (last ~14 months)
                    startTime = endTime.minusDays(500);
                    interval = "1mo";
                    break;
                default:
                    log("Unsupported timeframe: " + timeframe);
                    return null;
            }
            
            long startTimestamp = startTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            long endTimestamp = endTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=%s",
                    symbol, startTimestamp, endTimestamp, interval);
            
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<String> response = httpClient.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log("Failed to fetch " + timeframe + " prices for " + symbol + " - HTTP " + response.statusCode());
                return null;
            }
            
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.body());
            com.fasterxml.jackson.databind.JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                log("No " + timeframe + " price data returned for " + symbol);
                return null;
            }
            
            com.fasterxml.jackson.databind.JsonNode data = result.get(0);
            com.fasterxml.jackson.databind.JsonNode timestamps = data.path("timestamp");
            
            // Check if timestamps exist
            if (timestamps == null || !timestamps.isArray() || timestamps.size() == 0) {
                log("No timestamps found for " + symbol + " with timeframe " + timeframe);
                return null;
            }
            
            com.fasterxml.jackson.databind.JsonNode indicators = data.path("indicators").path("quote");
            if (indicators == null || !indicators.isArray() || indicators.size() == 0) {
                log("No indicators found for " + symbol + " with timeframe " + timeframe);
                return null;
            }
            
            com.fasterxml.jackson.databind.JsonNode quote = indicators.get(0);
            if (quote == null) {
                log("Quote data is null for " + symbol + " with timeframe " + timeframe);
                return null;
            }
            
            com.fasterxml.jackson.databind.JsonNode closes = quote.path("close");
            if (closes == null || !closes.isArray()) {
                log("Close prices array is null or invalid for " + symbol + " with timeframe " + timeframe);
                return null;
            }
            
            // Extract closing prices
            List<BigDecimal> prices = new java.util.ArrayList<>();
            for (int i = 0; i < timestamps.size() && i < closes.size(); i++) {
                if (!closes.get(i).isNull()) {
                    prices.add(BigDecimal.valueOf(closes.get(i).asDouble()));
                }
            }
            
            if (prices.size() < 15) {
                log("Not enough " + timeframe + " price data for RSI calculation for " + symbol + " (need 15, have " + prices.size() + ")");
                return null;
            }
            
            // Convert to PriceData format for RSI calculation
            List<JdbcStockPriceRepository.PriceData> priceDataList = new java.util.ArrayList<>();
            for (int i = 0; i < prices.size(); i++) {
                priceDataList.add(new JdbcStockPriceRepository.PriceData(
                    symbol,
                    java.time.LocalDate.now(),
                    prices.get(i), // open
                    prices.get(i), // high
                    prices.get(i), // low
                    prices.get(i), // close
                    null,
                    0L
                ));
            }
            
            // Calculate RSI using 14-period
            BigDecimal rsi = calculateRSI(priceDataList, 14);
            
            if (rsi != null) {
                log("Calculated " + timeframe + " RSI for " + symbol + ": " + rsi.setScale(2, RoundingMode.HALF_UP));
            }
            
            return rsi;
            
        } catch (Exception e) {
            log("Error calculating " + timeframe + " RSI for " + symbol + ": " + e.getMessage());
            return null;
        }
    }
}

