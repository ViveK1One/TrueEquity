# TrueEquity

Stock analysis platform with a Java Spring Boot backend for data ingestion and a Next.js frontend for analysis and recommendations.

---

## Prerequisites

- **Java 17+**
- **Maven (mvnd 1.0.3)**
- **Node.js 18+ and npm**

---

## Step-by-Step Setup (Windows)

### Step 1 — Open the Project

Open the `TrueEquity` project folder in **VS Code** (or any IDE).

### Step 2 — Check What Is Already Installed

Open a terminal (PowerShell) inside the IDE and run:

```powershell
java -version
node -v
mvn -version
```

If all three commands return a version number, skip to **Step 6**. Otherwise, install whatever is missing using the steps below.

### Step 3 — Install Java 17 (if missing)

1. Download the installer from: https://adoptium.net/temurin/releases/?version=17
2. Run the installer.
3. **During installation, select the option to add Java to the system PATH automatically.**
4. After installation, close and reopen the terminal, then verify:

```powershell
java -version
```

### Step 4 — Install Node.js (if missing)

1. Download the LTS installer from: https://nodejs.org/en/download
2. Run the installer and complete the setup.
3. Add the Node.js bin path to the **System Environment Variables**:
   - Open **Start → search "Environment Variables" → Edit the system environment variables → Environment Variables**.
   - Under **System variables**, select `Path` and click **Edit**.
   - Click **New** and add:

```
C:\Program Files\nodejs\node_modules\npm\bin
```

4. Close and reopen the terminal, then verify:

```powershell
node -v
npm -v
```

### Step 5 — Install Maven (if missing)

Maven requires a few extra steps because it comes as a zip file.

1. Download the Maven zip from:
   https://dlcdn.apache.org/maven/mvnd/1.0.3/maven-mvnd-1.0.3-windows-amd64.zip

2. **Unzip** the downloaded file to a folder (e.g. your `Downloads` folder).

3. Add the Maven `bin` path to the **System Environment Variables**:
   - Open **Start → search "Environment Variables" → Edit the system environment variables → Environment Variables**.
   - Under **System variables**, select `Path` and click **Edit**.
   - Click **New** and add the path to the `mvn\bin` folder inside the unzipped directory, for example:

```
C:\Users\<your-username>\Downloads\maven-mvnd-1.0.3-windows-amd64\maven-mvnd-1.0.3-windows-amd64\mvn\bin
```

   Replace `<your-username>` with your actual Windows username.

4. Set the Maven executable path in VS Code:
   - In VS Code, press **Ctrl + ,** to open Settings.
   - Search for **maven executable path**.
   - Paste the full path to `mvnd.exe`, for example:

```
C:\Users\<your-username>\Downloads\maven-mvnd-1.0.3-windows-amd64\maven-mvnd-1.0.3-windows-amd64\bin\mvnd.exe
```

   Replace `<your-username>` with your actual Windows username.

5. **Close VS Code completely** and reopen it, then verify in the terminal:

```powershell
mvn -version
```

### Step 6 — Create the Environment File

Create a file named `.env.local` inside the `frontend/` folder, using `frontend/.env.local.example` as a template. Copy the example file and rename the copy to `.env.local`, then fill in the values with the details provided on the title slide of the project presentation (database and app URLs supplied by the author for evaluation).

### Step 7 — Run the Backend

Open a terminal in VS Code at the **project root** (`TrueEquity/`) and run:

```powershell
mvn clean install
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**.

### Step 8 — Run the Frontend

Open a **second terminal** in VS Code, navigate to the frontend folder, install dependencies, and start the dev server:

```powershell
cd frontend
npm install
npm run dev
```

The frontend will start on **http://localhost:3000**.

### Step 9 — Open the App

Open **http://localhost:3000** in your browser. Both services must be running at the same time.

---

## Common Issues

- **`java: command not found`**
  Make sure Java is installed and added to the system PATH. Restart the terminal after installation.

- **`mvn: command not found`**
  Make sure the Maven `mvn\bin` path is added to the system PATH and VS Code has been restarted.

- **`node: command not found`**
  Make sure Node.js is installed and its bin path is added to the system PATH.

- **Port 8080 already in use**
  Another instance of the backend is running. Find and kill it:

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
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
└── database/              SQL schema
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
