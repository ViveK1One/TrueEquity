package com.trueequity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TrueEquity Data Ingestion Service
 * 
 * Background service for fetching stock market data from APIs,
 * processing it, and storing in PostgreSQL database.
 * 
 * Runs 24/7 independently of user requests.
 */
@SpringBootApplication
@EnableScheduling
public class TrueEquityIngestionApplication {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static void log(String message) {
        System.out.println(LocalDateTime.now().format(FORMATTER) + " - " + message);
    }

    public static void main(String[] args) {
        // Disable Spring Boot banner
        System.setProperty("spring.main.banner-mode", "off");
        
        log("Starting TrueEquity Data Ingestion Service...");
        SpringApplication.run(TrueEquityIngestionApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log("Application started successfully");
        // Next run times will be printed by scheduler
    }
}

