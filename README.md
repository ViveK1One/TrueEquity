# TrueEquity

Stock analysis platform with a Java backend for data ingestion and a Next.js frontend for analysis and recommendations.

---

## Overview

TrueEquity is a full-stack application that fetches stock data from market APIs, computes valuation and risk scores, and serves a web UI with BUY/HOLD/SELL/AVOID recommendations. The backend runs as an independent service; the frontend connects to it and to PostgreSQL for fast queries.

**This repository contains:**
- **Backend** – Java Spring Boot data ingestion service (this root)
- **Frontend** – Next.js app in the `frontend/` directory

---

## Technology Stack

| Layer      | Technologies |
|-----------|---------------|
| Backend   | Java 17, Spring Boot 3.2.0, Spring Scheduler, JDBC |
| Database  | PostgreSQL |
| Frontend  | Next.js 14+, TypeScript, Tailwind CSS |

---

## Backend (Data Ingestion Service)

The backend runs independently and:
- Fetches stock data from financial APIs (prices, volume, fundamentals)
- Normalizes and stores data in PostgreSQL
- Pre-computes scores (valuation, health, growth, risk)
- Keeps data updated for fast frontend queries

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (database: `trueequity_market_data`)

### Database Setup

1. Create the database:

```sql
CREATE DATABASE trueequity_market_data;
```

2. Apply the schema:

```bash
psql -U postgres -d trueequity_market_data -f database/schema.sql
```

### Configuration

Edit `src/main/resources/application.properties` and set your database URL, username, and password. The stock list (NASDAQ 100 subset) is also configured there.

### Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

### Scheduled Jobs

- **Price updates** – Every 15 minutes during market hours (9:30 AM–4:00 PM EST)
- **Fundamentals update** – Daily at 6 PM EST
- **Score recalculation** – Every hour

### API Providers

- **Yahoo Finance** (primary, no API key required)
- **Alpha Vantage** (fallback, API key required)

Additional providers can be added by implementing the `DataProvider` interface.

### Data Flow

```
API Provider → Data Ingestion Service → Database
                        ↓
                Metrics Calculation Service
                        ↓
                Pre-computed scores (for frontend)
```

### Project Structure

```
src/main/java/com/trueequity/
├── api/dto/           Data transfer objects
├── api/provider/      API provider implementations
├── config/            Configuration
├── controller/        REST API controllers
├── repository/        Data access (JDBC)
├── scheduler/         Background jobs
├── service/           Business logic
├── util/              Utilities
└── TrueEquityIngestionApplication.java
```

### Database Tables (main)

- `stocks` – Basic stock info  
- `stock_prices` – Daily OHLCV (partitioned by year)  
- `stock_financials` – Fundamentals (quarterly/annual)  
- `stock_scores` – Pre-computed scores  
- `technical_indicators` – e.g. RSI  

---

## Frontend

The web UI is in the `frontend/` folder. To run it:

```bash
cd frontend
npm install
```

Create `frontend/.env.local` with your database and `NEXT_PUBLIC_APP_URL` settings, then:

```bash
npm run dev
```

---

## Disclaimer

This platform is for informational and educational purposes only. Nothing herein constitutes investment advice, a recommendation, or an offer or solicitation to buy or sell any security. All investment decisions are made solely at your own risk. Past performance does not guarantee future results.

---

## License

© 2026 TrueEquity. All rights reserved.
