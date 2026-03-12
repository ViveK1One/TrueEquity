# TrueEquity

Stock analysis platform with a Java Spring Boot backend for data ingestion and a Next.js frontend for analysis and recommendations.

---

## What you need

- Java 17+
- Maven 3.6+
- Node.js 18+ and npm

The setup script checks and installs all of these automatically if missing.

---

## Quick Start (Recommended)

Run the setup script — it installs missing dependencies, creates the environment file, builds the backend, and starts both services.

**Windows:**
```bat
setup.bat
```

**macOS / Linux:**
```bash
chmod +x setup.sh
./setup.sh
```

The script will:
1. Check and install Java 17 if missing
2. Check and install Maven if missing
3. Check and install Node.js 20 if missing
4. Create `frontend/.env.local` from the template below
5. Install frontend dependencies (`npm install`)
6. Build the backend (`mvn clean install`)
7. Start both the backend (port 8080) and frontend (port 3000)

Open **http://localhost:3000** in your browser once both services are running.

---

## Manual Setup (step by step)

If you prefer to run each step yourself:

**Terminal 1 – Backend:**
```bash
cd TrueEquity
mvn clean install
mvn spring-boot:run
```

**Terminal 2 – Frontend:**
```bash
cd TrueEquity/frontend
npm install
npm run dev
```

Backend: http://localhost:8080  
Frontend: http://localhost:3000

---

## Environment File

Create `frontend/.env.local` with the following variables (use given database credentials):

```env
DATABASE_HOST=<your-database-host>
DATABASE_PORT=5432
DATABASE_NAME=<your-database-name>
DATABASE_USER=<your-database-user>
DATABASE_PASSWORD=<your-database-password>
JAVA_BACKEND_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

Database credentials are supplied by the author for evaluation. The backend reads the connection from `backend/main/resources/application.properties`.

---

## Common Issues

- **`java: command not found` or `mvn: command not found`**  
  Run `setup.bat` (Windows) or `setup.sh` (macOS/Linux) — it installs these automatically.

- **Port 8080 already in use**  
  Another instance of the backend is running. On Windows:
  ```bat
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  ```
  On macOS/Linux:
  ```bash
  lsof -ti:8080 | xargs kill -9
  ```

---

## Overview

TrueEquity is a full-stack application that fetches stock data from market APIs, computes valuation and risk scores, and serves a web UI with BUY/HOLD/SELL/AVOID recommendations. The backend runs as an independent service; the frontend connects to it and to PostgreSQL for fast queries.

**This repository contains:**
- **Backend** – Java Spring Boot data ingestion service (project root)
- **Frontend** – Next.js app in the `frontend/` directory
- **Database** – Schema in `database/schema.sql` (already applied to the hosted database)

---

## Technology Stack

| Layer     | Technologies                                  |
|-----------|-----------------------------------------------|
| Backend   | Java 17, Spring Boot 3.2.0, Spring Scheduler, JDBC |
| Database  | PostgreSQL (hosted on Supabase)               |
| Frontend  | Next.js 14+, TypeScript, Tailwind CSS         |
| APIs      | Yahoo Finance, FMP (Financial Modeling Prep)  |

---

## Project Structure

```
TrueEquity/
├── backend/main/java/com/trueequity/
│   ├── api/dto/           Data transfer objects
│   ├── api/provider/      API provider implementations (Yahoo, FMP, Hybrid)
│   ├── config/            Database and scheduler configuration
│   ├── controller/        REST API controllers
│   ├── repository/        Data access (JDBC)
│   ├── scheduler/         Background scheduled jobs
│   ├── service/           Business logic (scores, RSI, ingestion)
│   ├── util/              Utility classes
│   └── TrueEquityIngestionApplication.java
├── frontend/              Next.js web application
├── database/              SQL schema
├── setup.bat              Windows auto-setup and launch script
└── setup.sh               macOS/Linux auto-setup and launch script
```

---

## Data Flow

```
Yahoo Finance / FMP API
        ↓
  Data Ingestion Service (Spring Boot)
        ↓
  Metrics Calculation Service
        ↓
  PostgreSQL (Supabase) ← → Next.js Frontend
```

---

## Scheduled Jobs

| Job                  | Schedule                              |
|----------------------|---------------------------------------|
| Price updates        | Every 15 minutes during market hours (9:30 AM–4:00 PM EST) |
| Fundamentals update  | Daily at 6 PM EST                     |
| Score recalculation  | Every hour                            |

---

## Database Tables

| Table                     | Description                                  |
|---------------------------|----------------------------------------------|
| `stocks`                  | Basic stock info                             |
| `stock_prices`            | Daily OHLCV prices (partitioned by year)    |
| `stock_prices_intraday`  | Intraday prices (partitioned)                |
| `stock_financials`        | Fundamentals (quarterly/annual)              |
| `stock_scores`            | Pre-computed valuation scores                |
| `technical_indicators`    | RSI and other indicators                     |
| `market_data`             | Market data                                  |
| `historical_snapshots`    | Historical snapshots                         |
| `stock_predictions`       | Stock predictions                            |
| `strategy_backtests`      | Strategy backtest results                    |
| `ingestion_log`           | Ingestion run log                            |

---

## Tests

From the project root:

```bash
mvn test
```

Runs backend unit and API tests: `MetricsCalculationServiceTest`, `TechnicalIndicatorServiceTest`, `RSIControllerTest`.

---

## Disclaimer

This platform is for informational and educational purposes only. Nothing herein constitutes investment advice, a recommendation, or an offer or solicitation to buy or sell any security. All investment decisions are made solely at your own risk. Past performance does not guarantee future results.

---

## License

© 2026 TrueEquity. All rights reserved.

---

Academic project for DLBCSPJWD01 (IU). Developed by Vivek Dudhat.
