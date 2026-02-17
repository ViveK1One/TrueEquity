-- ============================================================================
-- TrueEquity Stock Market Data Database Schema
-- Optimized for: ML Strategy Building, Backtesting, Fast Frontend Queries
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- CORE STOCK INFORMATION
-- ============================================================================

CREATE TABLE stocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    exchange VARCHAR(50) NOT NULL DEFAULT 'NASDAQ',
    sector VARCHAR(100),
    industry VARCHAR(100),
    market_cap BIGINT, -- Market capitalization in USD
    is_active BOOLEAN DEFAULT TRUE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for fast lookups
    CONSTRAINT stocks_symbol_check CHECK (symbol = UPPER(symbol))
);

CREATE INDEX idx_stocks_symbol ON stocks(symbol);
CREATE INDEX idx_stocks_sector ON stocks(sector);
CREATE INDEX idx_stocks_industry ON stocks(industry);
CREATE INDEX idx_stocks_active ON stocks(is_active);

-- ============================================================================
-- DAILY PRICE DATA (Partitioned by year for performance)
-- ============================================================================

CREATE TABLE stock_prices (
    id UUID DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    open DECIMAL(12, 4) NOT NULL,
    high DECIMAL(12, 4) NOT NULL,
    low DECIMAL(12, 4) NOT NULL,
    close DECIMAL(12, 4) NOT NULL,
    adjusted_close DECIMAL(12, 4), -- Adjusted for splits/dividends
    volume BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    PRIMARY KEY (symbol, date)
) PARTITION BY RANGE (date);

-- Create partitions for current and next year (extend as needed)
CREATE TABLE stock_prices_2024 PARTITION OF stock_prices
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
CREATE TABLE stock_prices_2025 PARTITION OF stock_prices
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE stock_prices_2026 PARTITION OF stock_prices
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

-- Indexes for fast queries
CREATE INDEX idx_stock_prices_symbol_date ON stock_prices(symbol, date DESC);
CREATE INDEX idx_stock_prices_date ON stock_prices(date DESC);

-- ============================================================================
-- INTRADAY PRICE DATA (For minute-by-minute updates & ML features)
-- ============================================================================

CREATE TABLE stock_prices_intraday (
    id UUID DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    open DECIMAL(12, 4) NOT NULL,
    high DECIMAL(12, 4) NOT NULL,
    low DECIMAL(12, 4) NOT NULL,
    close DECIMAL(12, 4) NOT NULL,
    volume BIGINT NOT NULL,
    interval_minutes INT DEFAULT 1, -- 1, 5, 15, 30, 60 minutes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    PRIMARY KEY (symbol, timestamp, interval_minutes)
) PARTITION BY RANGE (timestamp);

-- Create monthly partitions for intraday data (high volume)
CREATE TABLE stock_prices_intraday_2024_12 PARTITION OF stock_prices_intraday
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');
CREATE TABLE stock_prices_intraday_2025_01 PARTITION OF stock_prices_intraday
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE stock_prices_intraday_2025_02 PARTITION OF stock_prices_intraday
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

CREATE INDEX idx_intraday_symbol_timestamp ON stock_prices_intraday(symbol, timestamp DESC);
CREATE INDEX idx_intraday_timestamp ON stock_prices_intraday(timestamp DESC);

-- ============================================================================
-- FUNDAMENTAL DATA (Quarterly & Annual)
-- ============================================================================

CREATE TABLE stock_financials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- 'quarterly' or 'annual'
    period_end_date DATE NOT NULL,
    fiscal_year INT,
    fiscal_quarter INT,
    
    -- Valuation Metrics
    pe_ratio DECIMAL(10, 4),
    peg_ratio DECIMAL(10, 4),
    price_to_book DECIMAL(10, 4),
    price_to_sales DECIMAL(10, 4),
    ev_to_ebitda DECIMAL(10, 4),
    
    -- Earnings
    eps_ttm DECIMAL(12, 4),
    eps_growth_yoy DECIMAL(10, 4), -- Year-over-year EPS growth %
    eps_growth_qoq DECIMAL(10, 4), -- Quarter-over-quarter EPS growth %
    
    -- Revenue & Profit
    revenue BIGINT, -- Total revenue
    revenue_growth_yoy DECIMAL(10, 4),
    revenue_growth_qoq DECIMAL(10, 4),
    net_income BIGINT,
    net_income_growth_yoy DECIMAL(10, 4),
    profit_margin DECIMAL(10, 4), -- Net income / Revenue
    
    -- Balance Sheet
    total_cash BIGINT,
    total_debt BIGINT,
    cash_per_share DECIMAL(10, 4),
    debt_to_equity DECIMAL(10, 4),
    current_ratio DECIMAL(10, 4),
    
    -- Profitability
    roe DECIMAL(10, 4), -- Return on Equity
    roic DECIMAL(10, 4), -- Return on Invested Capital
    roa DECIMAL(10, 4), -- Return on Assets
    gross_margin DECIMAL(10, 4),
    operating_margin DECIMAL(10, 4),
    
    -- Growth Metrics
    revenue_growth_3y DECIMAL(10, 4), -- 3-year average growth
    earnings_growth_3y DECIMAL(10, 4),
    
    -- Market Data
    shares_outstanding BIGINT,
    float_shares BIGINT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    UNIQUE(symbol, period_type, period_end_date)
);

CREATE INDEX idx_financials_symbol_period ON stock_financials(symbol, period_end_date DESC);
CREATE INDEX idx_financials_period_type ON stock_financials(period_type);

-- ============================================================================
-- CALCULATED SCORES & METRICS (Pre-computed for fast frontend queries)
-- ============================================================================

CREATE TABLE stock_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Valuation Category: 'cheap', 'fair', 'expensive'
    valuation_category VARCHAR(20),
    valuation_score DECIMAL(5, 2), -- 0-100 scale
    
    -- Financial Health Score (0-100)
    health_score DECIMAL(5, 2),
    health_grade VARCHAR(2), -- A, B, C, D, F
    
    -- Growth Score (0-100)
    growth_score DECIMAL(5, 2),
    growth_grade VARCHAR(2),
    
    -- Risk Score (0-100, higher = more risk)
    risk_score DECIMAL(5, 2),
    risk_grade VARCHAR(2),
    
    -- Overall Score
    overall_score DECIMAL(5, 2),
    overall_grade VARCHAR(2),
    
    -- Score Components (for transparency)
    pe_score DECIMAL(5, 2),
    peg_score DECIMAL(5, 2), -- PEG ratio score
    debt_score DECIMAL(5, 2),
    profitability_score DECIMAL(5, 2),
    growth_rate_score DECIMAL(5, 2),
    volatility_score DECIMAL(5, 2),
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    UNIQUE(symbol, calculated_at)
);

CREATE INDEX idx_scores_symbol_latest ON stock_scores(symbol, calculated_at DESC);
CREATE INDEX idx_scores_overall ON stock_scores(overall_score DESC);

-- ============================================================================
-- TECHNICAL INDICATORS (For ML Strategy Building)
-- ============================================================================

CREATE TABLE technical_indicators (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    
    -- Moving Averages
    sma_20 DECIMAL(12, 4), -- Simple Moving Average 20 days
    sma_50 DECIMAL(12, 4),
    sma_200 DECIMAL(12, 4),
    ema_12 DECIMAL(12, 4), -- Exponential Moving Average
    ema_26 DECIMAL(12, 4),
    
    -- Momentum Indicators
    rsi DECIMAL(5, 2), -- Relative Strength Index (0-100)
    macd DECIMAL(12, 4), -- MACD line
    macd_signal DECIMAL(12, 4), -- Signal line
    macd_histogram DECIMAL(12, 4), -- Histogram
    stochastic_k DECIMAL(5, 2),
    stochastic_d DECIMAL(5, 2),
    
    -- Volatility
    bollinger_upper DECIMAL(12, 4),
    bollinger_middle DECIMAL(12, 4),
    bollinger_lower DECIMAL(12, 4),
    atr DECIMAL(12, 4), -- Average True Range
    
    -- Volume Indicators
    volume_sma_20 BIGINT,
    volume_ratio DECIMAL(10, 4), -- Current volume / Average volume
    
    -- Price Patterns
    price_change_pct DECIMAL(10, 4), -- Daily % change
    price_change_5d DECIMAL(10, 4), -- 5-day % change
    price_change_20d DECIMAL(10, 4), -- 20-day % change
    price_change_50d DECIMAL(10, 4), -- 50-day % change
    
    -- Support/Resistance Levels
    support_level DECIMAL(12, 4),
    resistance_level DECIMAL(12, 4),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    UNIQUE(symbol, date)
);

CREATE INDEX idx_indicators_symbol_date ON technical_indicators(symbol, date DESC);

-- ============================================================================
-- MARKET CONTEXT DATA (For correlation & sector analysis)
-- ============================================================================

CREATE TABLE market_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL UNIQUE,
    
    -- Major Indices
    sp500_close DECIMAL(12, 4),
    nasdaq_close DECIMAL(12, 4),
    dow_close DECIMAL(12, 4),
    
    -- Market Metrics
    vix DECIMAL(10, 4), -- Volatility Index
    market_sentiment VARCHAR(20), -- 'bullish', 'bearish', 'neutral'
    
    -- Sector Performance (JSON or separate table)
    sector_performance JSONB, -- Store sector-wise performance
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_market_data_date ON market_data(date DESC);

-- ============================================================================
-- HISTORICAL SNAPSHOTS (For Backtesting)
-- ============================================================================

CREATE TABLE historical_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    snapshot_date DATE NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    
    -- Price at snapshot
    price DECIMAL(12, 4) NOT NULL,
    volume BIGINT,
    
    -- All metrics at this point in time
    pe_ratio DECIMAL(10, 4),
    market_cap BIGINT,
    rsi DECIMAL(5, 2),
    macd DECIMAL(12, 4),
    
    -- Store full state as JSON for flexibility
    full_state JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE
);

CREATE INDEX idx_snapshots_symbol_date ON historical_snapshots(symbol, snapshot_date DESC);
CREATE INDEX idx_snapshots_date ON historical_snapshots(snapshot_date DESC);

-- ============================================================================
-- PREDICTION RESULTS (For storing ML predictions)
-- ============================================================================

CREATE TABLE stock_predictions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(10) NOT NULL,
    prediction_date DATE NOT NULL,
    prediction_time TIMESTAMP NOT NULL,
    target_date DATE NOT NULL, -- Date we're predicting for
    
    -- Predictions
    predicted_price DECIMAL(12, 4),
    predicted_change_pct DECIMAL(10, 4),
    confidence_score DECIMAL(5, 2), -- 0-100
    
    -- Model Info
    model_version VARCHAR(50),
    model_type VARCHAR(50), -- 'ml', 'statistical', 'hybrid'
    features_used JSONB, -- Features used in prediction
    
    -- Actual Results (filled later for model evaluation)
    actual_price DECIMAL(12, 4),
    actual_change_pct DECIMAL(10, 4),
    prediction_error DECIMAL(10, 4),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
    UNIQUE(symbol, prediction_date, target_date)
);

CREATE INDEX idx_predictions_symbol_date ON stock_predictions(symbol, prediction_date DESC);
CREATE INDEX idx_predictions_target_date ON stock_predictions(target_date);

-- ============================================================================
-- BACKTEST RESULTS (For strategy validation)
-- ============================================================================

CREATE TABLE strategy_backtests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    strategy_name VARCHAR(100) NOT NULL,
    strategy_version VARCHAR(50),
    backtest_start_date DATE NOT NULL,
    backtest_end_date DATE NOT NULL,
    
    -- Performance Metrics
    total_return DECIMAL(10, 4), -- %
    annualized_return DECIMAL(10, 4), -- %
    sharpe_ratio DECIMAL(10, 4),
    max_drawdown DECIMAL(10, 4), -- %
    win_rate DECIMAL(5, 2), -- %
    
    -- Trade Statistics
    total_trades INT,
    winning_trades INT,
    losing_trades INT,
    avg_profit_per_trade DECIMAL(10, 4),
    
    -- Strategy Parameters
    parameters JSONB, -- Store strategy parameters
    
    -- Results Data
    results_data JSONB, -- Store detailed backtest results
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_backtests_strategy ON strategy_backtests(strategy_name, created_at DESC);

-- ============================================================================
-- DATA INGESTION LOG (For monitoring & debugging)
-- ============================================================================

CREATE TABLE ingestion_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_type VARCHAR(50) NOT NULL, -- 'price_update', 'fundamentals_update', 'score_calculation'
    symbol VARCHAR(10),
    status VARCHAR(20) NOT NULL, -- 'success', 'failed', 'partial'
    records_processed INT DEFAULT 0,
    error_message TEXT,
    execution_time_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ingestion_log_created ON ingestion_log(created_at DESC);
CREATE INDEX idx_ingestion_log_symbol ON ingestion_log(symbol);
CREATE INDEX idx_ingestion_log_status ON ingestion_log(status);

-- ============================================================================
-- FUNCTIONS & TRIGGERS
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for stocks table
CREATE TRIGGER update_stocks_updated_at BEFORE UPDATE ON stocks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for stock_financials table
CREATE TRIGGER update_financials_updated_at BEFORE UPDATE ON stock_financials
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS (Documentation)
-- ============================================================================

COMMENT ON TABLE stocks IS 'Core stock information - NASDAQ 100 stocks';
COMMENT ON TABLE stock_prices IS 'Daily OHLCV data - partitioned by year for performance';
COMMENT ON TABLE stock_prices_intraday IS 'Intraday price data for minute-by-minute updates';
COMMENT ON TABLE stock_financials IS 'Quarterly and annual fundamental data';
COMMENT ON TABLE stock_scores IS 'Pre-computed scores for fast frontend queries';
COMMENT ON TABLE technical_indicators IS 'Technical indicators for ML strategy building';
COMMENT ON TABLE market_data IS 'Market-wide context data for correlation analysis';
COMMENT ON TABLE historical_snapshots IS 'Historical snapshots for backtesting strategies';
COMMENT ON TABLE stock_predictions IS 'ML model predictions with actual results for evaluation';
COMMENT ON TABLE strategy_backtests IS 'Backtest results for strategy validation';
COMMENT ON TABLE ingestion_log IS 'Logging table for monitoring data ingestion jobs';

