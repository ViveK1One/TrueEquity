package com.trueequity.api.provider;

import com.trueequity.api.dto.StockFundamentalDTO;
import com.trueequity.api.dto.StockInfoDTO;
import com.trueequity.api.dto.StockPriceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Financial Modeling Prep (FMP) API provider implementation
 * Provides comprehensive financial statements data
 * Free tier: 250 calls/day (10x better than Alpha Vantage)
 */
@Component
public class FinancialModelingPrepProvider implements DataProvider {

    private static final String BASE_URL = "https://financialmodelingprep.com/stable";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    // Rate limiting tracking
    private long lastCallTime = 0;
    private static final long MIN_CALL_INTERVAL_MS = 250; // ~4 calls per second to be safe
    private int dailyRequestCount = 0;
    private LocalDate currentDate = LocalDate.now();
    private static final int DAILY_REQUEST_LIMIT = 250;
    private boolean dailyLimitReached = false;

    public FinancialModelingPrepProvider(@Value("${app.data-provider.fmp.api-key:}") String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    private boolean waitForRateLimit() {
        // Reset daily counter if it's a new day
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            currentDate = today;
            dailyRequestCount = 0;
            dailyLimitReached = false;
            System.out.println("FMP: Daily request counter reset for " + today + " (250 requests available)");
        }
        
        // Check daily limit
        if (dailyRequestCount >= DAILY_REQUEST_LIMIT) {
            if (!dailyLimitReached) {
                System.out.println("⚠️  FMP: Daily limit of " + DAILY_REQUEST_LIMIT + " requests reached.");
                dailyLimitReached = true;
            }
            return false;
        }
        
        // Wait for rate limit
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastCallTime;
        
        if (timeSinceLastCall < MIN_CALL_INTERVAL_MS) {
            try {
                Thread.sleep(MIN_CALL_INTERVAL_MS - timeSinceLastCall);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastCallTime = System.currentTimeMillis();
        dailyRequestCount++;
        
        if (dailyRequestCount % 50 == 0) {
            System.out.println("FMP: " + dailyRequestCount + "/" + DAILY_REQUEST_LIMIT + " requests used today");
        }
        
        return true;
    }

    @Override
    public Optional<StockInfoDTO> getStockInfo(String symbol) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            if (!waitForRateLimit()) {
                return Optional.empty();
            }
            
            String url = BASE_URL + "/profile?symbol=" + symbol + "&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 403) {
                System.out.println("⚠️  FMP API Key issue for " + symbol + ": HTTP 403 Forbidden");
                System.out.println("    Please verify your API key is activated at: https://site.financialmodelingprep.com/developer/docs");
                System.out.println("    Response: " + response.body().substring(0, Math.min(200, response.body().length())));
                return Optional.empty();
            }
            
            if (response.statusCode() == 401) {
                System.out.println("⚠️  FMP API Key unauthorized for " + symbol + ": HTTP 401");
                return Optional.empty();
            }
            
            if (response.statusCode() != 200) {
                System.out.println("FMP API error for " + symbol + ": HTTP " + response.statusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            // FMP returns an array with one object
            if (root == null || !root.isArray() || root.size() == 0) {
                System.out.println("FMP: No profile data for " + symbol);
                return Optional.empty();
            }
            
            JsonNode data = root.get(0);
            if (data == null) {
                return Optional.empty();
            }
            
            // Null-safe: FMP stable API uses companyName, exchangeShortName, marketCap (not mktCap)
            String name = data.has("companyName") ? data.get("companyName").asText("") : "";
            if (name == null || name.isEmpty() || "null".equals(name)) {
                return Optional.empty();
            }
            
            StockInfoDTO info = new StockInfoDTO();
            info.setSymbol(symbol.toUpperCase());
            info.setName(name);
            info.setExchange(data.has("exchangeShortName") ? data.get("exchangeShortName").asText("NASDAQ") : "NASDAQ");
            info.setSector(data.has("sector") && !data.get("sector").isNull() ? data.get("sector").asText("") : null);
            info.setIndustry(data.has("industry") && !data.get("industry").isNull() ? data.get("industry").asText("") : null);
            
            // FMP stable returns marketCap (double); legacy was mktCap
            long marketCap = 0;
            if (data.has("marketCap") && !data.get("marketCap").isNull()) {
                marketCap = (long) data.get("marketCap").asDouble(0);
            } else if (data.has("mktCap") && !data.get("mktCap").isNull()) {
                marketCap = data.get("mktCap").asLong(0);
            }
            if (marketCap > 0) {
                info.setMarketCap(marketCap);
            }
            
            return Optional.of(info);
            
        } catch (Exception e) {
            String msg = e.getMessage();
            System.out.println("Exception fetching stock info from FMP for " + symbol + ": " + (msg != null ? msg : e.getClass().getSimpleName()));
            return Optional.empty();
        }
    }

    @Override
    public List<StockPriceDTO> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        // Use Yahoo Finance for historical prices (faster, no rate limits)
        return new ArrayList<>();
    }

    @Override
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        // Use Yahoo Finance for current price
        return Optional.empty();
    }

    @Override
    public Optional<StockFundamentalDTO> getFundamentals(String symbol) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }

        try {
            // Per FMP official docs: Income Statement, Balance Sheet, Profile, Key Metrics (for P/E and ratios)
            // 1. Income Statement (annual) - revenue, net income, EPS, growth
            if (!waitForRateLimit()) {
                System.out.println("FMP: Skipping fundamentals for " + symbol + " - rate/daily limit reached");
                return Optional.empty();
            }
            Optional<JsonNode> incomeStatement = fetchIncomeStatement(symbol);
            if (incomeStatement.isEmpty()) {
                System.out.println("FMP: No income statement returned for " + symbol + " - cannot build fundamentals");
                return Optional.empty();
            }
            
            // 2. Balance Sheet (annual) - cash, debt, equity, assets, liabilities
            if (!waitForRateLimit()) return Optional.empty();
            Optional<JsonNode> balanceSheet = fetchBalanceSheet(symbol);
            
            // 3. Profile - sector, industry, shares, market cap (per Company Profile Data API)
            if (!waitForRateLimit()) return Optional.empty();
            Optional<JsonNode> profile = fetchProfile(symbol);
            
            // 4. Key Metrics (official P/E, valuation ratios per Key Metrics API docs)
            if (!waitForRateLimit()) return Optional.empty();
            Optional<JsonNode> keyMetrics = fetchKeyMetrics(symbol);
            
            // Build DTO from the data we have
            StockFundamentalDTO dto = buildFundamentalDTO(symbol, 
                    incomeStatement.get(), 
                    balanceSheet.orElse(null),
                    profile.orElse(null),
                    keyMetrics.orElse(null));
            
            return Optional.of(dto);
            
        } catch (Exception e) {
            System.out.println("Error fetching fundamentals from FMP for " + symbol + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    private Optional<JsonNode> fetchProfile(String symbol) {
        try {
            String url = BASE_URL + "/profile?symbol=" + symbol + "&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchIncomeStatement(String symbol) {
        try {
            String url = BASE_URL + "/income-statement?symbol=" + symbol + "&limit=2&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.out.println("FMP income-statement for " + symbol + ": HTTP " + response.statusCode() + " - " + response.body().substring(0, Math.min(300, response.body().length())));
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                System.out.println("FMP income-statement for " + symbol + ": empty array in response");
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            System.out.println("Error fetching income statement from FMP for " + symbol + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchBalanceSheet(String symbol) {
        try {
            String url = BASE_URL + "/balance-sheet-statement?symbol=" + symbol + "&limit=1&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchCashFlow(String symbol) {
        try {
            String url = BASE_URL + "/cash-flow-statement?symbol=" + symbol + "&limit=1&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchKeyMetrics(String symbol) {
        try {
            String url = BASE_URL + "/key-metrics?symbol=" + symbol + "&limit=1&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchFinancialRatios(String symbol) {
        try {
            String url = BASE_URL + "/ratios?symbol=" + symbol + "&limit=1&apikey=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            
            if (!root.isArray() || root.size() == 0) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private StockFundamentalDTO buildFundamentalDTO(String symbol, JsonNode incomeStatement, 
            JsonNode balanceSheet, JsonNode profile, JsonNode keyMetrics) {
        
        StockFundamentalDTO dto = new StockFundamentalDTO();
        dto.setSymbol(symbol.toUpperCase());
        dto.setPeriodType("annual");
        
        // Get latest annual report
        JsonNode incomeData = incomeStatement.get(0);
        
        // Period End Date
        String dateStr = incomeData.path("date").asText();
        if (!dateStr.isEmpty()) {
            try {
                dto.setPeriodEndDate(LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE));
                dto.setFiscalYear(dto.getPeriodEndDate().getYear());
            } catch (Exception e) {
                dto.setPeriodEndDate(LocalDate.now());
                dto.setFiscalYear(LocalDate.now().getYear());
            }
        }
        
        // Income Statement Data
        long revenue = incomeData.path("revenue").asLong(0);
        if (revenue > 0) {
            dto.setRevenue(revenue);
        }
        
        long netIncome = incomeData.path("netIncome").asLong(0);
        if (netIncome != 0) {
            dto.setNetIncome(netIncome);
        }
        
        long grossProfit = incomeData.path("grossProfit").asLong(0);
        long operatingIncome = incomeData.path("operatingIncome").asLong(0);
        
        // Calculate margins from income statement
        if (revenue > 0) {
            // Profit Margin
            if (netIncome != 0) {
                BigDecimal margin = BigDecimal.valueOf(netIncome)
                    .divide(BigDecimal.valueOf(revenue), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setProfitMargin(margin);
            }
            
            // Gross Margin
            if (grossProfit > 0) {
                BigDecimal grossMargin = BigDecimal.valueOf(grossProfit)
                    .divide(BigDecimal.valueOf(revenue), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setGrossMargin(grossMargin);
            }
            
            // Operating Margin
            if (operatingIncome != 0) {
                BigDecimal operatingMargin = BigDecimal.valueOf(operatingIncome)
                    .divide(BigDecimal.valueOf(revenue), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setOperatingMargin(operatingMargin);
            }
        }
        
        // EPS from income statement
        double eps = incomeData.path("eps").asDouble(0);
        if (eps != 0) {
            dto.setEpsTtm(BigDecimal.valueOf(eps));
        }
        
        // Balance Sheet Data
        if (balanceSheet != null && balanceSheet.size() > 0) {
            JsonNode balanceData = balanceSheet.get(0);
            
            long totalCash = balanceData.path("cashAndCashEquivalents").asLong(0);
            if (totalCash > 0) {
                dto.setTotalCash(totalCash);
            }
            
            long totalDebt = balanceData.path("totalDebt").asLong(0);
            if (totalDebt >= 0) {  // Can be 0
                dto.setTotalDebt(totalDebt);
            }
            
            long totalEquity = balanceData.path("totalStockholdersEquity").asLong(0);
            long totalAssets = balanceData.path("totalAssets").asLong(0);
            long currentAssets = balanceData.path("totalCurrentAssets").asLong(0);
            long currentLiabilities = balanceData.path("totalCurrentLiabilities").asLong(0);
            
            // Debt to Equity
            if (totalDebt >= 0 && totalEquity > 0) {
                BigDecimal debtToEquity = BigDecimal.valueOf(totalDebt)
                    .divide(BigDecimal.valueOf(totalEquity), 4, RoundingMode.HALF_UP);
                dto.setDebtToEquity(debtToEquity);
            }
            
            // Current Ratio
            if (currentAssets > 0 && currentLiabilities > 0) {
                BigDecimal currentRatio = BigDecimal.valueOf(currentAssets)
                    .divide(BigDecimal.valueOf(currentLiabilities), 4, RoundingMode.HALF_UP);
                dto.setCurrentRatio(currentRatio);
            }
            
            // Calculate ROE (Return on Equity) - store as percentage (e.g. 15 for 15%)
            if (netIncome != 0 && totalEquity > 0) {
                BigDecimal roe = BigDecimal.valueOf(netIncome)
                    .divide(BigDecimal.valueOf(totalEquity), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setRoe(roe);
            }
            
            // Calculate ROA (Return on Assets) - store as percentage
            if (netIncome != 0 && totalAssets > 0) {
                BigDecimal roa = BigDecimal.valueOf(netIncome)
                    .divide(BigDecimal.valueOf(totalAssets), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setRoa(roa);
            }
            
            // Calculate ROIC (Return on Invested Capital) = Operating Income / (Total Equity + Total Debt)
            long investedCapital = totalEquity + totalDebt;
            if (operatingIncome != 0 && investedCapital > 0) {
                BigDecimal roic = BigDecimal.valueOf(operatingIncome)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(investedCapital), 4, RoundingMode.HALF_UP);
                dto.setRoic(roic);
            }
        }
        
        // Key Metrics (official FMP Key Metrics API - P/E, valuation ratios per docs)
        if (keyMetrics != null && keyMetrics.size() > 0) {
            JsonNode km = keyMetrics.get(0);
            // P/E Ratio - official Key Metrics API field (peRatio or priceEarningsRatio)
            double pe = km.path("peRatio").asDouble(0);
            if (pe <= 0 && km.has("priceEarningsRatio")) pe = km.get("priceEarningsRatio").asDouble(0);
            if (pe <= 0 && km.has("pe")) pe = km.get("pe").asDouble(0);
            if (pe > 0) dto.setPeRatio(BigDecimal.valueOf(pe));
            // Price to Book - key-metrics may provide priceToBookRatio
            double ptb = km.path("priceToBookRatio").asDouble(0);
            if (ptb > 0) dto.setPriceToBook(BigDecimal.valueOf(ptb));
            // PEG - if provided
            double peg = km.path("pegRatio").asDouble(0);
            if (peg > 0) dto.setPegRatio(BigDecimal.valueOf(peg));
        }
        
        // Profile Data (Company Profile API - market cap, shares, sector; PE fallback if not in key-metrics)
        if (profile != null && profile.size() > 0) {
            JsonNode profileData = profile.get(0);
            if (profileData == null) {
                profileData = objectMapper.createObjectNode();
            }
            
            // PE from profile only when not already set by Key Metrics API
            if (dto.getPeRatio() == null) {
                double peRatio = profileData.path("peRatio").asDouble(0);
                if (peRatio <= 0 && profileData.has("pe")) {
                    peRatio = profileData.get("pe").asDouble(0);
                }
                if (peRatio > 0) {
                    dto.setPeRatio(BigDecimal.valueOf(peRatio));
                }
            }
            // Shares Outstanding
            long shares = profileData.path("sharesOutstanding").asLong(0);
            if (shares > 0) {
                dto.setSharesOutstanding(shares);
            }
            
            // Market Cap - FMP stable returns as double
            long marketCap = 0;
            if (profileData.has("marketCap") && !profileData.get("marketCap").isNull()) {
                marketCap = (long) profileData.get("marketCap").asDouble(0);
            } else if (profileData.has("mktCap")) {
                marketCap = profileData.path("mktCap").asLong(0);
            }
            // Fallback: PE = Market Cap / Net Income when profile does not provide PE
            if (dto.getPeRatio() == null && marketCap > 0 && netIncome > 0) {
                dto.setPeRatio(BigDecimal.valueOf(marketCap)
                        .divide(BigDecimal.valueOf(netIncome), 4, RoundingMode.HALF_UP));
            }
            if (marketCap > 0 && revenue > 0) {
                // Price to Sales
                BigDecimal priceToSales = BigDecimal.valueOf(marketCap)
                    .divide(BigDecimal.valueOf(revenue), 4, RoundingMode.HALF_UP);
                dto.setPriceToSales(priceToSales);
            }
            
            // Price to Book = Market Cap / Total Shareholders' Equity (from balance sheet)
            if (marketCap > 0 && balanceSheet != null && balanceSheet.size() > 0) {
                long totalEquity = balanceSheet.get(0).path("totalStockholdersEquity").asLong(0);
                if (totalEquity > 0) {
                    BigDecimal priceToBook = BigDecimal.valueOf(marketCap)
                        .divide(BigDecimal.valueOf(totalEquity), 4, RoundingMode.HALF_UP);
                    dto.setPriceToBook(priceToBook);
                }
            }
        }
        
        // EPS growth YoY and PEG = PE / (EPS growth %) from 2 years of income
        if (incomeStatement.size() > 1) {
            double epsCurrent = incomeStatement.get(0).path("eps").asDouble(0);
            double epsPrior = incomeStatement.get(1).path("eps").asDouble(0);
            if (epsPrior != 0) {
                double epsGrowthPercent = (epsCurrent - epsPrior) / epsPrior * 100;
                dto.setEpsGrowthYoy(BigDecimal.valueOf(epsGrowthPercent).setScale(4, RoundingMode.HALF_UP));
                if (epsGrowthPercent > 0 && dto.getPeRatio() != null) {
                    BigDecimal peg = dto.getPeRatio()
                        .divide(BigDecimal.valueOf(epsGrowthPercent), 4, RoundingMode.HALF_UP);
                    dto.setPegRatio(peg);
                }
            }
        }
        
        // Revenue Growth (calculate from data if we have 2+ years)
        if (incomeStatement.size() > 1) {
            long currentRevenue = incomeStatement.get(0).path("revenue").asLong(0);
            long previousRevenue = incomeStatement.get(1).path("revenue").asLong(0);
            if (currentRevenue > 0 && previousRevenue > 0) {
                BigDecimal growth = BigDecimal.valueOf(currentRevenue - previousRevenue)
                    .divide(BigDecimal.valueOf(previousRevenue), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setRevenueGrowthYoy(growth);
            }
        }
        
        return dto;
    }

    @Override
    public String getProviderName() {
        return "Financial Modeling Prep";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
