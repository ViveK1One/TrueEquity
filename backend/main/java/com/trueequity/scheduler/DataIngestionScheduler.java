package com.trueequity.scheduler;

import com.trueequity.service.DataIngestionService;
import com.trueequity.service.TechnicalIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Background scheduler for 24/7 data ingestion
 * Runs independently of user requests
 */
@Component
@RequiredArgsConstructor
public class DataIngestionScheduler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static void log(String message) {
        System.out.println(LocalDateTime.now().format(FORMATTER) + " - " + message);
    }

    private final DataIngestionService dataIngestionService;
    private final TechnicalIndicatorService technicalIndicatorService;

    @Value("${app.stocks.list}")
    private String stocksList;

    @Value("${app.market.open-hour:9}")
    private int marketOpenHour;

    @Value("${app.market.open-minute:30}")
    private int marketOpenMinute;

    @Value("${app.market.close-hour:16}")
    private int marketCloseHour;

    /**
     * Update stock prices every 15 minutes during market hours
     * Cron: minute hour day month day-of-week
     * Runs at :00, :15, :30, :45 of every hour during market hours
     */
    @Scheduled(cron = "0 0,15,30,45 9-16 * * MON-FRI", zone = "America/New_York")
    public void updateStockPrices() {
        if (!isMarketOpen()) {
            log("Market is closed, skipping price update");
            return;
        }

        log("Starting scheduled price update...");
        List<String> symbols = getStockSymbols();
        log("Processing " + symbols.size() + " stocks");

        for (String symbol : symbols) {
            try {
                // Update price (includes volume)
                dataIngestionService.ingestLatestPrice(symbol);
                Thread.sleep(100);
                
                // Calculate and store RSI for all timeframes (1h, 30m, 2h, 1d)
                technicalIndicatorService.calculateAndStoreRSIForAllTimeframes(symbol);
                Thread.sleep(50);
                
                // Update fundamentals (includes EPS) - updates every 15 min like prices
                dataIngestionService.ingestFundamentals(symbol);
                Thread.sleep(200);
                
            } catch (Exception e) {
                log("Error updating price/RSI/EPS for " + symbol + ": " + e.getMessage());
            }
        }

        log("Completed scheduled price and RSI update");
    }
    
    /**
     * Initial data load job - runs once on startup, immediately (no delay).
     * Processes ALL stocks from configuration.
     * After this completes, the existing scheduled jobs (price, fundamentals, score) run at their normal times.
     */
    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 0)
    public void initialDataLoadOnStartup() {
        log("Starting immediate data load for all stocks (runs once on boot)...");
        List<String> symbols = getStockSymbols();
        log("Total stocks to process: " + symbols.size());

        int successCount = 0;
        int failCount = 0;

        for (String symbol : symbols) {
            try {
                log("Processing stock: " + symbol);

                // Force full refresh on boot (no skip-by-threshold)
                dataIngestionService.ingestStockInfo(symbol, true);
                Thread.sleep(300);

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(60);
                dataIngestionService.ingestHistoricalPrices(symbol, startDate, endDate);
                Thread.sleep(300);

                technicalIndicatorService.calculateAndStoreRSIForAllTimeframes(symbol);
                Thread.sleep(200);

                dataIngestionService.ingestFundamentals(symbol, true);
                Thread.sleep(300);

                dataIngestionService.calculateAndStoreScores(symbol, true);
                Thread.sleep(200);

                successCount++;
                log("Completed processing: " + symbol);

            } catch (Exception e) {
                log("Failed to process " + symbol + ": " + e.getMessage());
                failCount++;
            }
        }

        log("Initial data load completed: " + successCount + " succeeded, " + failCount + " failed");
        log("Next runs will follow the existing schedule (see below).");

        ZonedDateTime nowEST = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime nextPrice = calculateNextPriceUpdate(nowEST);
        ZonedDateTime nextFundamentals = calculateNextFundamentalsUpdate(nowEST);
        // Score job uses cron without zone → server default; show next full hour in server time
        ZonedDateTime nowServer = ZonedDateTime.now();
        ZonedDateTime nextScore = nowServer.withMinute(0).withSecond(0).withNano(0);
        if (!nextScore.isAfter(nowServer)) nextScore = nextScore.plusHours(1);

        log("Next price update (15 min during market hours): " + nextPrice.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Next fundamentals update (daily 6 PM EST):        " + nextFundamentals.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Next score recalculation (hourly):               " + nextScore.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log("Background schedulers are active");
        
        // Calculate and show next run times
        ZonedDateTime nowEST = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime nowGermany = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        
        // Market hours info
        log("Market hours: 9:30 AM - 4:00 PM EST (3:30 PM - 10:00 PM Germany time)");
        log("Current time EST: " + nowEST.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Current time Germany: " + nowGermany.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        // Next price update (every 15 min during market hours)
        ZonedDateTime nextPriceUpdate = calculateNextPriceUpdate(nowEST);
        ZonedDateTime nextPriceUpdateGermany = nextPriceUpdate.withZoneSameInstant(ZoneId.of("Europe/Berlin"));
        log("Price updates: Every 15 minutes during market hours");
        log("Next price update EST: " + nextPriceUpdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Next price update Germany: " + nextPriceUpdateGermany.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        // Next fundamentals update (6 PM EST daily)
        ZonedDateTime nextFundamentals = calculateNextFundamentalsUpdate(nowEST);
        ZonedDateTime nextFundamentalsGermany = nextFundamentals.withZoneSameInstant(ZoneId.of("Europe/Berlin"));
        log("Fundamentals update: Daily at 6 PM EST (12:00 AM next day Germany time)");
        log("Next fundamentals update EST: " + nextFundamentals.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Next fundamentals update Germany: " + nextFundamentalsGermany.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        // Next score recalculation (every hour)
        ZonedDateTime nextScoreCalc = nowEST.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextScoreCalcGermany = nextScoreCalc.withZoneSameInstant(ZoneId.of("Europe/Berlin"));
        log("Score recalculation: Every hour");
        log("Next score recalculation EST: " + nextScoreCalc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        log("Next score recalculation Germany: " + nextScoreCalcGermany.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        log("Waiting for scheduled jobs to execute...");
    }
    
    private ZonedDateTime calculateNextPriceUpdate(ZonedDateTime now) {
        LocalTime marketOpen = LocalTime.of(marketOpenHour, marketOpenMinute);
        LocalTime marketClose = LocalTime.of(marketCloseHour, 0);
        LocalTime nowTime = now.toLocalTime();
        
        // Check if it's a weekday (Monday-Friday)
        int dayOfWeek = now.getDayOfWeek().getValue();
        boolean isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;
        
        if (!isWeekday) {
            // If weekend, next is Monday at market open
            int daysUntilMonday = 8 - dayOfWeek;
            return now.plusDays(daysUntilMonday).withHour(marketOpenHour).withMinute(marketOpenMinute).withSecond(0).withNano(0);
        }
        
        // If market is open, next update is in 15 minutes (rounded up)
        if (nowTime.isAfter(marketOpen) && nowTime.isBefore(marketClose)) {
            int currentMinute = now.getMinute();
            int nextMinute = ((currentMinute / 15) + 1) * 15;
            if (nextMinute >= 60) {
                return now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            }
            return now.withMinute(nextMinute).withSecond(0).withNano(0);
        }
        
        // If before market open, next is at market open today
        if (nowTime.isBefore(marketOpen)) {
            return now.withHour(marketOpenHour).withMinute(marketOpenMinute).withSecond(0).withNano(0);
        }
        
        // If after market close, next is tomorrow at market open
        return now.plusDays(1).withHour(marketOpenHour).withMinute(marketOpenMinute).withSecond(0).withNano(0);
    }
    
    private ZonedDateTime calculateNextFundamentalsUpdate(ZonedDateTime now) {
        LocalTime updateTime = LocalTime.of(18, 0); // 6 PM
        LocalTime nowTime = now.toLocalTime();
        
        // Check if it's a weekday
        int dayOfWeek = now.getDayOfWeek().getValue();
        boolean isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;
        
        if (isWeekday && nowTime.isBefore(updateTime)) {
            // Today at 6 PM
            return now.withHour(18).withMinute(0).withSecond(0).withNano(0);
        } else {
            // Next weekday at 6 PM
            int daysToAdd = 1;
            if (!isWeekday) {
                // If weekend, go to Monday
                daysToAdd = 8 - dayOfWeek;
            } else if (nowTime.isAfter(updateTime)) {
                // If after 6 PM on weekday, go to next weekday
                if (dayOfWeek == 5) { // Friday
                    daysToAdd = 3; // Monday
                } else {
                    daysToAdd = 1; // Next day
                }
            }
            return now.plusDays(daysToAdd).withHour(18).withMinute(0).withSecond(0).withNano(0);
        }
    }

    /**
     * Update fundamentals daily after market close
     * Runs at 6 PM EST (after market closes at 4 PM)
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "America/New_York")
    public void updateFundamentals() {
        log("Starting scheduled fundamentals update...");
        List<String> symbols = getStockSymbols();

        for (String symbol : symbols) {
            try {
                dataIngestionService.ingestFundamentals(symbol);
                Thread.sleep(500); // Longer delay for fundamentals (rate limiting)
            } catch (Exception e) {
                log("Error updating fundamentals for " + symbol + ": " + e.getMessage());
            }
        }

        log("Completed scheduled fundamentals update");
    }

    /**
     * Recalculate scores every hour
     * Ensures scores are always up-to-date for frontend queries
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void recalculateScores() {
        log("Starting scheduled score recalculation...");
        List<String> symbols = getStockSymbols();

        for (String symbol : symbols) {
            try {
                dataIngestionService.calculateAndStoreScores(symbol);
            } catch (Exception e) {
                log("Error recalculating scores for " + symbol + ": " + e.getMessage());
            }
        }

        log("Completed scheduled score recalculation");
    }

    /**
     * Initial data load - fetch historical data for all stocks
     * Runs once on startup (can be triggered manually)
     */
    public void initialDataLoad() {
        log("Starting initial data load...");
        List<String> symbols = getStockSymbols();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(2); // Load 2 years of history

        for (String symbol : symbols) {
            try {
                log("Loading initial data for: " + symbol);
                
                // Load stock info
                dataIngestionService.ingestStockInfo(symbol);
                
                // Load historical prices
                dataIngestionService.ingestHistoricalPrices(symbol, startDate, endDate);
                
                // Load fundamentals
                dataIngestionService.ingestFundamentals(symbol);
                
                // Calculate initial scores
                dataIngestionService.calculateAndStoreScores(symbol);
                
                Thread.sleep(1000); // Delay to avoid rate limiting
                
            } catch (Exception e) {
                log("Error in initial data load for " + symbol + ": " + e.getMessage());
            }
        }

        log("Completed initial data load");
    }

    /**
     * Check if market is currently open
     */
    private boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        LocalTime open = LocalTime.of(marketOpenHour, marketOpenMinute);
        LocalTime close = LocalTime.of(marketCloseHour, 0);
        
        return now.isAfter(open) && now.isBefore(close);
    }

    /**
     * Get list of stock symbols from configuration
     */
    private List<String> getStockSymbols() {
        return Arrays.stream(stocksList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

