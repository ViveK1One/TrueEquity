package com.trueequity.api.provider;

import com.trueequity.api.dto.StockFundamentalDTO;
import com.trueequity.api.dto.StockInfoDTO;
import com.trueequity.api.dto.StockPriceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Yahoo Finance API provider implementation
 * Uses yahoo-finance API endpoints (free, no API key required)
 */
@Component
public class YahooFinanceProvider implements DataProvider {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<StockInfoDTO> getStockInfo(String symbol) {
        try {
            // Use quote endpoint for better company info (includes sector, industry, market cap)
            String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbol;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                // Fallback to chart endpoint if quote fails
                return getStockInfoFromChart(symbol);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("quoteResponse").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                // Fallback to chart endpoint
                return getStockInfoFromChart(symbol);
            }

            JsonNode quote = result.get(0);
            
            String name = quote.path("longName").asText();
            if (name == null || name.isEmpty()) {
                name = quote.path("shortName").asText();
            }
            String exchange = quote.path("fullExchangeName").asText();
            if (exchange == null || exchange.isEmpty()) {
                exchange = quote.path("exchange").asText();
            }
            String sector = quote.path("sector").asText();
            String industry = quote.path("industry").asText();
            long marketCap = quote.path("marketCap").asLong(0);
            
            // Validate - don't return if essential data is missing
            if (name == null || name.isEmpty() || name.equals("null")) {
                // Fallback to chart endpoint
                return getStockInfoFromChart(symbol);
            }
            
            StockInfoDTO info = new StockInfoDTO();
            info.setSymbol(symbol.toUpperCase());
            info.setName(name);
            info.setExchange(exchange != null && !exchange.isEmpty() ? exchange : "NASDAQ");
            info.setSector(sector != null && !sector.isEmpty() && !sector.equals("null") ? sector : null);
            info.setIndustry(industry != null && !industry.isEmpty() && !industry.equals("null") ? industry : null);
            info.setMarketCap(marketCap > 0 ? marketCap : null);
            
            return Optional.of(info);
            
        } catch (Exception e) {
            // Fallback to chart endpoint
            return getStockInfoFromChart(symbol);
        }
    }
    
    /**
     * Fallback method using chart endpoint (has less info but more reliable)
     */
    private Optional<StockInfoDTO> getStockInfoFromChart(String symbol) {
        try {
            String url = BASE_URL + symbol + "?interval=1d&range=1d";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                return Optional.empty();
            }

            JsonNode data = result.get(0);
            JsonNode meta = data.path("meta");
            
            String name = meta.path("longName").asText();
            if (name == null || name.isEmpty()) {
                name = meta.path("shortName").asText();
            }
            String exchange = meta.path("exchangeName").asText();
            String sector = meta.path("sector").asText();
            String industry = meta.path("industry").asText();
            long marketCap = meta.path("marketCap").asLong(0);
            
            // Validate - don't return if essential data is missing
            if (name == null || name.isEmpty() || name.equals("null")) {
                return Optional.empty();
            }
            
            StockInfoDTO info = new StockInfoDTO();
            info.setSymbol(symbol.toUpperCase());
            info.setName(name);
            info.setExchange(exchange != null && !exchange.isEmpty() ? exchange : "NASDAQ");
            info.setSector(sector != null && !sector.isEmpty() && !sector.equals("null") ? sector : null);
            info.setIndustry(industry != null && !industry.isEmpty() && !industry.equals("null") ? industry : null);
            info.setMarketCap(marketCap > 0 ? marketCap : null);
            
            return Optional.of(info);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<StockPriceDTO> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        List<StockPriceDTO> prices = new ArrayList<>();
        
        try {
            long startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long endTimestamp = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            
            String url = String.format("%s%s?period1=%d&period2=%d&interval=1d", 
                    BASE_URL, symbol, startTimestamp, endTimestamp);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return prices;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                return prices;
            }

            JsonNode data = result.get(0);
            JsonNode timestamps = data.path("timestamp");
            JsonNode indicators = data.path("indicators").path("quote").get(0);
            
            JsonNode opens = indicators.path("open");
            JsonNode highs = indicators.path("high");
            JsonNode lows = indicators.path("low");
            JsonNode closes = indicators.path("close");
            JsonNode volumes = indicators.path("volume");
            
            JsonNode adjCloses = data.path("indicators").path("adjclose");
            boolean hasAdjClose = adjCloses.isArray() && adjCloses.size() > 0;
            
            for (int i = 0; i < timestamps.size(); i++) {
                long timestamp = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                // Skip if any essential price data is null
                if (!opens.has(i) || !highs.has(i) || !lows.has(i) || !closes.has(i) || !volumes.has(i)) {
                    continue;
                }
                
                BigDecimal open = BigDecimal.valueOf(opens.get(i).asDouble());
                BigDecimal high = BigDecimal.valueOf(highs.get(i).asDouble());
                BigDecimal low = BigDecimal.valueOf(lows.get(i).asDouble());
                BigDecimal close = BigDecimal.valueOf(closes.get(i).asDouble());
                long volume = volumes.get(i).asLong();
                
                // Validate - skip if any price is zero or negative
                if (open.compareTo(BigDecimal.ZERO) <= 0 || 
                    high.compareTo(BigDecimal.ZERO) <= 0 || 
                    low.compareTo(BigDecimal.ZERO) <= 0 || 
                    close.compareTo(BigDecimal.ZERO) <= 0 ||
                    volume <= 0) {
                    continue;
                }
                
                BigDecimal adjustedClose = null;
                if (hasAdjClose && adjCloses.get(0).path("adjclose").has(i)) {
                    adjustedClose = BigDecimal.valueOf(adjCloses.get(0).path("adjclose").get(i).asDouble());
                }
                
                StockPriceDTO price = new StockPriceDTO();
                price.setSymbol(symbol.toUpperCase());
                price.setDate(date);
                price.setOpen(open);
                price.setHigh(high);
                price.setLow(low);
                price.setClose(close);
                price.setAdjustedClose(adjustedClose);
                price.setVolume(volume);
                
                prices.add(price);
            }
            
        } catch (Exception e) {
            // Error handling - return empty list
        }
        
        return prices;
    }

    @Override
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        try {
            String url = BASE_URL + symbol + "?interval=1d&range=1d";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                return Optional.empty();
            }

            JsonNode data = result.get(0);
            JsonNode meta = data.path("meta");
            double regularPrice = meta.path("regularMarketPrice").asDouble(0);
            
            if (regularPrice <= 0) {
                return Optional.empty();
            }
            
            return Optional.of(BigDecimal.valueOf(regularPrice));
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<StockFundamentalDTO> getFundamentals(String symbol) {
        try {
            // Use chart endpoint (v8) - same as we use for prices, no authentication needed
            // The meta section contains fundamental data like PE, EPS, market cap, etc.
            String url = BASE_URL + symbol + "?interval=1d&range=1d";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("chart").path("result");
            
            if (!result.isArray() || result.size() == 0) {
                return Optional.empty();
            }

            JsonNode data = result.get(0);
            JsonNode meta = data.path("meta");
            
            StockFundamentalDTO dto = new StockFundamentalDTO();
            dto.setSymbol(symbol.toUpperCase());
            dto.setPeriodType("annual");
            dto.setPeriodEndDate(LocalDate.now());
            
            // PE Ratio (same as Python: info.get('trailingPE') or info.get('forwardPE'))
            if (meta.has("trailingPE") && !meta.path("trailingPE").isNull()) {
                dto.setPeRatio(BigDecimal.valueOf(meta.path("trailingPE").asDouble()));
            } else if (meta.has("forwardPE") && !meta.path("forwardPE").isNull()) {
                dto.setPeRatio(BigDecimal.valueOf(meta.path("forwardPE").asDouble()));
            }
            
            // PEG Ratio - calculate if we have PE
            BigDecimal peRatio = dto.getPeRatio();
            if (peRatio != null && meta.has("earningsQuarterlyGrowth") && !meta.path("earningsQuarterlyGrowth").isNull()) {
                double earningsGrowth = meta.path("earningsQuarterlyGrowth").asDouble();
                if (earningsGrowth != 0) {
                    if (Math.abs(earningsGrowth) < 1) {
                        earningsGrowth = earningsGrowth * 100;
                    }
                    if (earningsGrowth > 0) {
                        dto.setPegRatio(peRatio.divide(BigDecimal.valueOf(earningsGrowth), 4, RoundingMode.HALF_UP));
                    }
                }
            }
            
            // Price to Book
            if (meta.has("priceToBook") && !meta.path("priceToBook").isNull()) {
                dto.setPriceToBook(BigDecimal.valueOf(meta.path("priceToBook").asDouble()));
            }
            
            // Price to Sales
            if (meta.has("priceToSalesTrailing12Months") && !meta.path("priceToSalesTrailing12Months").isNull()) {
                dto.setPriceToSales(BigDecimal.valueOf(meta.path("priceToSalesTrailing12Months").asDouble()));
            }
            
            // EV to EBITDA
            if (meta.has("enterpriseToEbitda") && !meta.path("enterpriseToEbitda").isNull()) {
                dto.setEvToEbitda(BigDecimal.valueOf(meta.path("enterpriseToEbitda").asDouble()));
            }
            
            // Earnings (same as Python: info.get('trailingEps') or info.get('forwardEps'))
            if (meta.has("trailingEPS") && !meta.path("trailingEPS").isNull()) {
                dto.setEpsTtm(BigDecimal.valueOf(meta.path("trailingEPS").asDouble()));
            } else if (meta.has("forwardEPS") && !meta.path("forwardEPS").isNull()) {
                dto.setEpsTtm(BigDecimal.valueOf(meta.path("forwardEPS").asDouble()));
            }
            
            // Shares Outstanding
            if (meta.has("sharesOutstanding") && !meta.path("sharesOutstanding").isNull()) {
                dto.setSharesOutstanding(meta.path("sharesOutstanding").asLong());
            }
            
            // Float Shares
            if (meta.has("floatShares") && !meta.path("floatShares").isNull()) {
                dto.setFloatShares(meta.path("floatShares").asLong());
            }
            
            // Check if we got at least some data - if PE or EPS exists, return it
            if (dto.getPeRatio() != null || dto.getEpsTtm() != null || dto.getSharesOutstanding() != null) {
                return Optional.of(dto);
            } else {
                return Optional.empty();
            }
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public String getProviderName() {
        return "Yahoo Finance";
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
