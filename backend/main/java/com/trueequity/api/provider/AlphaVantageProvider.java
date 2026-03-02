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
 * Alpha Vantage API provider implementation
 * Provides comprehensive financial statements data
 * Free tier: 500 calls/day, 5 calls/minute
 */
@Component
public class AlphaVantageProvider implements DataProvider {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    // Rate limiting: 5 calls/minute = 12 seconds between calls
    private long lastCallTime = 0;
    private static final long MIN_CALL_INTERVAL_MS = 12000;

    public AlphaVantageProvider(@Value("${app.data-provider.alpha-vantage.api-key:}") String apiKey) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    /**
     * Rate limit: wait if needed to respect 5 calls/minute limit
     */
    private void waitForRateLimit() {
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
    }

    @Override
    public Optional<StockInfoDTO> getStockInfo(String symbol) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            waitForRateLimit();
            Optional<JsonNode> overview = fetchOverview(symbol);
            
            if (overview.isEmpty()) {
                return Optional.empty();
            }
            
            JsonNode data = overview.get();
            
            // Check if we got valid data
            if (data.isEmpty() || data.path("Symbol").asText().isEmpty()) {
                return Optional.empty();
            }
            
            StockInfoDTO info = new StockInfoDTO();
            info.setSymbol(symbol.toUpperCase());
            info.setName(data.path("Name").asText());
            info.setExchange(data.path("Exchange").asText());
            
            // Get sector, industry, market cap from Alpha Vantage
            String sector = data.path("Sector").asText();
            if (!sector.isEmpty() && !sector.equals("None")) {
                info.setSector(sector);
            }
            
            String industry = data.path("Industry").asText();
            if (!industry.isEmpty() && !industry.equals("None")) {
                info.setIndustry(industry);
            }
            
            String marketCapStr = data.path("MarketCapitalization").asText();
            if (!marketCapStr.isEmpty() && !marketCapStr.equals("None")) {
                try {
                    info.setMarketCap(Long.parseLong(marketCapStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Validate essential fields
            if (info.getName() == null || info.getName().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(info);
            
        } catch (Exception e) {
            System.out.println("Exception fetching stock info from Alpha Vantage for " + symbol + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<StockPriceDTO> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        // Alpha Vantage has rate limits, use Yahoo Finance for prices
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
            waitForRateLimit();
            
            // Fetch Income Statement
            Optional<JsonNode> incomeStatement = fetchIncomeStatement(symbol);
            if (incomeStatement.isEmpty()) {
                return Optional.empty();
            }
            
            waitForRateLimit();
            
            // Fetch Balance Sheet
            Optional<JsonNode> balanceSheet = fetchBalanceSheet(symbol);
            
            waitForRateLimit();
            
            // Fetch Cash Flow
            Optional<JsonNode> cashFlow = fetchCashFlow(symbol);
            
            waitForRateLimit();
            
            // Fetch Overview (for ratios)
            Optional<JsonNode> overview = fetchOverview(symbol);
            
            // Build DTO from all data
            StockFundamentalDTO dto = buildFundamentalDTO(symbol, incomeStatement.get(), 
                    balanceSheet.orElse(null), cashFlow.orElse(null), overview.orElse(null));
            
            return Optional.of(dto);
            
        } catch (Exception e) {
            System.out.println("Error fetching fundamentals from Alpha Vantage for " + symbol + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchIncomeStatement(String symbol) {
        try {
            String url = BASE_URL + "?function=INCOME_STATEMENT&symbol=" + symbol + "&apikey=" + apiKey;
            
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
            
            // Check for rate limit message
            if (root.has("Note") && root.get("Note").asText().contains("API call frequency")) {
                System.out.println("Alpha Vantage rate limit reached, waiting...");
                Thread.sleep(60000); // Wait 1 minute
                return Optional.empty();
            }
            
            // Check for error
            if (root.has("Error Message")) {
                System.out.println("Alpha Vantage error: " + root.get("Error Message").asText());
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            System.out.println("Error fetching income statement: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchBalanceSheet(String symbol) {
        try {
            String url = BASE_URL + "?function=BALANCE_SHEET&symbol=" + symbol + "&apikey=" + apiKey;
            
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
            
            if (root.has("Note") || root.has("Error Message")) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchCashFlow(String symbol) {
        try {
            String url = BASE_URL + "?function=CASH_FLOW&symbol=" + symbol + "&apikey=" + apiKey;
            
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
            
            if (root.has("Note") || root.has("Error Message")) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<JsonNode> fetchOverview(String symbol) {
        try {
            String url = BASE_URL + "?function=OVERVIEW&symbol=" + symbol + "&apikey=" + apiKey;
            
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
            
            if (root.has("Note") || root.has("Error Message")) {
                return Optional.empty();
            }
            
            return Optional.of(root);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private StockFundamentalDTO buildFundamentalDTO(String symbol, JsonNode incomeStatement, 
            JsonNode balanceSheet, JsonNode cashFlow, JsonNode overview) {
        
        StockFundamentalDTO dto = new StockFundamentalDTO();
        dto.setSymbol(symbol.toUpperCase());
        dto.setPeriodType("annual");
        
        // Get latest annual report date
        LocalDate periodEndDate = LocalDate.now();
        if (incomeStatement != null && incomeStatement.has("annualReports") && incomeStatement.get("annualReports").isArray()) {
            JsonNode reports = incomeStatement.get("annualReports");
            if (reports.size() > 0) {
                String dateStr = reports.get(0).path("fiscalDateEnding").asText();
                try {
                    periodEndDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                } catch (Exception e) {
                    // Use current date if parsing fails
                }
            }
        }
        dto.setPeriodEndDate(periodEndDate);
        
        // Extract fiscal year from date
        dto.setFiscalYear(periodEndDate.getYear());
        dto.setFiscalQuarter(null);
        
        // Income Statement Data
        if (incomeStatement != null && incomeStatement.has("annualReports") && incomeStatement.get("annualReports").isArray()) {
            JsonNode latestReport = incomeStatement.get("annualReports").get(0);
            
            // Revenue
            String revenueStr = latestReport.path("totalRevenue").asText();
            if (!revenueStr.isEmpty() && !revenueStr.equals("None")) {
                try {
                    dto.setRevenue(Long.parseLong(revenueStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Net Income
            String netIncomeStr = latestReport.path("netIncome").asText();
            if (!netIncomeStr.isEmpty() && !netIncomeStr.equals("None")) {
                try {
                    dto.setNetIncome(Long.parseLong(netIncomeStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Calculate profit margin
            if (dto.getRevenue() != null && dto.getNetIncome() != null && dto.getRevenue() > 0) {
                BigDecimal margin = BigDecimal.valueOf(dto.getNetIncome())
                    .divide(BigDecimal.valueOf(dto.getRevenue()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setProfitMargin(margin);
            }
        }
        
        // Balance Sheet Data
        if (balanceSheet != null && balanceSheet.has("annualReports") && balanceSheet.get("annualReports").isArray()) {
            JsonNode latestReport = balanceSheet.get("annualReports").get(0);
            
            // Total Cash
            String cashStr = latestReport.path("cashAndShortTermInvestments").asText();
            if (cashStr.isEmpty() || cashStr.equals("None")) {
                cashStr = latestReport.path("cashAndCashEquivalentsAtCarryingValue").asText();
            }
            if (!cashStr.isEmpty() && !cashStr.equals("None")) {
                try {
                    dto.setTotalCash(Long.parseLong(cashStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Total Debt - try multiple fields
            String debtStr = latestReport.path("totalDebt").asText();
            if (debtStr.isEmpty() || debtStr.equals("None")) {
                // Try short term debt + long term debt
                String shortTermDebt = latestReport.path("shortTermDebt").asText();
                String longTermDebt = latestReport.path("longTermDebt").asText();
                if ((!shortTermDebt.isEmpty() && !shortTermDebt.equals("None")) || 
                    (!longTermDebt.isEmpty() && !longTermDebt.equals("None"))) {
                    try {
                        long shortTerm = shortTermDebt.isEmpty() || shortTermDebt.equals("None") ? 0 : Long.parseLong(shortTermDebt);
                        long longTerm = longTermDebt.isEmpty() || longTermDebt.equals("None") ? 0 : Long.parseLong(longTermDebt);
                        dto.setTotalDebt(shortTerm + longTerm);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            } else {
                try {
                    dto.setTotalDebt(Long.parseLong(debtStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Calculate debt to equity (use the totalDebt we already set)
            String equityStr = latestReport.path("totalShareholderEquity").asText();
            Long totalDebtValue = dto.getTotalDebt();
            if (totalDebtValue != null && totalDebtValue > 0 && !equityStr.isEmpty() && !equityStr.equals("None")) {
                try {
                    long equity = Long.parseLong(equityStr);
                    if (equity > 0) {
                        BigDecimal debtToEquity = BigDecimal.valueOf(totalDebtValue)
                            .divide(BigDecimal.valueOf(equity), 4, RoundingMode.HALF_UP);
                        dto.setDebtToEquity(debtToEquity);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Current Ratio
            String currentAssetsStr = latestReport.path("totalCurrentAssets").asText();
            String currentLiabilitiesStr = latestReport.path("totalCurrentLiabilities").asText();
            if (!currentAssetsStr.isEmpty() && !currentAssetsStr.equals("None") && 
                !currentLiabilitiesStr.isEmpty() && !currentLiabilitiesStr.equals("None")) {
                try {
                    long assets = Long.parseLong(currentAssetsStr);
                    long liabilities = Long.parseLong(currentLiabilitiesStr);
                    if (liabilities > 0) {
                        BigDecimal currentRatio = BigDecimal.valueOf(assets)
                            .divide(BigDecimal.valueOf(liabilities), 4, RoundingMode.HALF_UP);
                        dto.setCurrentRatio(currentRatio);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Overview Data (Ratios)
        if (overview != null) {
            // PE Ratio
            String peStr = overview.path("PERatio").asText();
            if (!peStr.isEmpty() && !peStr.equals("None")) {
                try {
                    dto.setPeRatio(new BigDecimal(peStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // PEG Ratio
            String pegStr = overview.path("PEGRatio").asText();
            if (!pegStr.isEmpty() && !pegStr.equals("None")) {
                try {
                    dto.setPegRatio(new BigDecimal(pegStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Price to Book
            String pbStr = overview.path("PriceToBookRatio").asText();
            if (!pbStr.isEmpty() && !pbStr.equals("None")) {
                try {
                    dto.setPriceToBook(new BigDecimal(pbStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Price to Sales
            String psStr = overview.path("PriceToSalesRatioTTM").asText();
            if (!psStr.isEmpty() && !psStr.equals("None")) {
                try {
                    dto.setPriceToSales(new BigDecimal(psStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // EV to EBITDA
            String evEbitdaStr = overview.path("EVToEBITDA").asText();
            if (!evEbitdaStr.isEmpty() && !evEbitdaStr.equals("None")) {
                try {
                    dto.setEvToEbitda(new BigDecimal(evEbitdaStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // EPS TTM
            String epsStr = overview.path("EPS").asText();
            if (!epsStr.isEmpty() && !epsStr.equals("None")) {
                try {
                    dto.setEpsTtm(new BigDecimal(epsStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Shares Outstanding
            String sharesStr = overview.path("SharesOutstanding").asText();
            if (!sharesStr.isEmpty() && !sharesStr.equals("None")) {
                try {
                    dto.setSharesOutstanding(Long.parseLong(sharesStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // ROE
            String roeStr = overview.path("ReturnOnEquityTTM").asText();
            if (!roeStr.isEmpty() && !roeStr.equals("None")) {
                try {
                    dto.setRoe(new BigDecimal(roeStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // ROIC
            String roicStr = overview.path("ReturnOnInvestedCapitalTTM").asText();
            if (!roicStr.isEmpty() && !roicStr.equals("None")) {
                try {
                    dto.setRoic(new BigDecimal(roicStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            } else {
                // Try alternative field name
                roicStr = overview.path("ReturnOnInvestedCapital").asText();
                if (!roicStr.isEmpty() && !roicStr.equals("None")) {
                    try {
                        dto.setRoic(new BigDecimal(roicStr));
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
            
            // ROA
            String roaStr = overview.path("ReturnOnAssetsTTM").asText();
            if (!roaStr.isEmpty() && !roaStr.equals("None")) {
                try {
                    dto.setRoa(new BigDecimal(roaStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Gross Margin
            String grossMarginStr = overview.path("GrossProfitTTM").asText();
            String revenueTTMStr = overview.path("RevenueTTM").asText();
            if (!grossMarginStr.isEmpty() && !grossMarginStr.equals("None") && 
                !revenueTTMStr.isEmpty() && !revenueTTMStr.equals("None")) {
                try {
                    long grossProfit = Long.parseLong(grossMarginStr);
                    long revenue = Long.parseLong(revenueTTMStr);
                    if (revenue > 0) {
                        BigDecimal grossMargin = BigDecimal.valueOf(grossProfit)
                            .divide(BigDecimal.valueOf(revenue), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        dto.setGrossMargin(grossMargin);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Operating Margin
            String operatingMarginStr = overview.path("OperatingMarginTTM").asText();
            if (!operatingMarginStr.isEmpty() && !operatingMarginStr.equals("None")) {
                try {
                    dto.setOperatingMargin(new BigDecimal(operatingMarginStr));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Revenue Growth
            String revenueGrowthStr = overview.path("QuarterlyRevenueGrowthYOY").asText();
            if (!revenueGrowthStr.isEmpty() && !revenueGrowthStr.equals("None")) {
                try {
                    BigDecimal growth = new BigDecimal(revenueGrowthStr);
                    dto.setRevenueGrowthYoy(growth.multiply(BigDecimal.valueOf(100))); // Convert to percentage
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            // Earnings Growth
            String earningsGrowthStr = overview.path("QuarterlyEarningsGrowthYOY").asText();
            if (!earningsGrowthStr.isEmpty() && !earningsGrowthStr.equals("None")) {
                try {
                    BigDecimal growth = new BigDecimal(earningsGrowthStr);
                    dto.setEpsGrowthYoy(growth.multiply(BigDecimal.valueOf(100))); // Convert to percentage
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        return dto;
    }

    @Override
    public String getProviderName() {
        return "Alpha Vantage";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
}

