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
- PostgreSQL (backend connects to the database configured in `application.properties`; data is read from this database)

### Configuration

Database connection (URL, username, password) is set in `src/main/resources/application.properties`, or via environment variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. No need to create a database or apply schema for evaluation—use the configured connection; data is served from this database.

### How to run the backend

1. Open a terminal and go to the **project root folder** (the folder that contains `pom.xml` and the `src` folder).  
   Example: `cd C:\Users\vivek\Downloads\TrueEquity` (or the path where you have the project).

2. Run:

```bash
mvn clean install
mvn spring-boot:run
```

This starts the application whose main class is **TrueEquityIngestionApplication** (file: `src/main/java/com/trueequity/TrueEquityIngestionApplication.java`). The backend runs on port 8080. Start it before the frontend.

### Scheduled Jobs

- **Price updates** – Every 15 minutes during market hours (9:30 AM–4:00 PM EST)
- **Fundamentals update** – Daily at 6 PM EST
- **Score recalculation** – Every hour

### API Providers

- **Yahoo Finance** (no API key required) – prices and some data
- **FMP (Financial Modeling Prep)** (API key in `application.properties`) – fundamentals, key metrics; used in a hybrid with Yahoo

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

**Prerequisites:** Node.js 18+ and npm.

The web UI is in the `frontend/` folder.

1. Install dependencies and run:

```bash
cd frontend
npm install
npm run dev
```

2. **Environment:** Create `frontend/.env.local` (optional; defaults work for local run) with:

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_HOST` | PostgreSQL host | `localhost` |
| `DATABASE_PORT` | PostgreSQL port | `5432` |
| `DATABASE_NAME` | Database name | `trueequity_market_data` |
| `DATABASE_USER` | Database user | `postgres` |
| `DATABASE_PASSWORD` | Database password | (set for your system) |
| `JAVA_BACKEND_URL` | Backend base URL (for RSI when not in DB) | `http://localhost:8080` |
| `NEXT_PUBLIC_APP_URL` | Frontend base URL (for API calls from browser) | `http://localhost:3000` |

Frontend runs at http://localhost:3000. Ensure the backend is running (port 8080) for full functionality (e.g. live RSI calculation fallback).

---

## Running the full application

1. Ensure PostgreSQL is running and the database connection in `application.properties` is correct (data is read from this database).
2. Start the backend: open terminal, go to project root, run `mvn spring-boot:run`.
3. Start the frontend: open another terminal, `cd frontend`, then `npm install` and `npm run dev`.
4. Open http://localhost:3000 in a browser.

---

## Tests

From the project root:

```bash
mvn test
```

Runs backend unit and API tests (e.g. `MetricsCalculationServiceTest`, `TechnicalIndicatorServiceTest`, `RSIControllerTest`).

---

## Disclaimer

This platform is for informational and educational purposes only. Nothing herein constitutes investment advice, a recommendation, or an offer or solicitation to buy or sell any security. All investment decisions are made solely at your own risk. Past performance does not guarantee future results.

---

## License

© 2026 TrueEquity. All rights reserved.
