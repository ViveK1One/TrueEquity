import { NextRequest, NextResponse } from 'next/server';
import pool from '@/lib/db';
import { calculateRecommendation } from '@/lib/recommendation';
import { StockDetailData } from '@/lib/types';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ symbol: string }> }
) {
  try {
    const { symbol: symbolParam } = await params;
    const symbol = symbolParam.toUpperCase();

    // Fetch stock basic info
    const stockResult = await pool.query(
      'SELECT * FROM stocks WHERE symbol = $1 AND is_active = true',
      [symbol]
    );

    if (stockResult.rows.length === 0) {
      return NextResponse.json(
        { error: 'Stock not found' },
        { status: 404 }
      );
    }

    const stock = stockResult.rows[0];

    // Fetch latest price
    const priceResult = await pool.query(
      `SELECT * FROM stock_prices 
       WHERE symbol = $1 
       ORDER BY date DESC 
       LIMIT 1`,
      [symbol]
    );
    const latestPrice = priceResult.rows[0] ? {
      ...priceResult.rows[0],
      close: parseFloat(priceResult.rows[0].close) || 0,
      open: parseFloat(priceResult.rows[0].open) || 0,
      high: parseFloat(priceResult.rows[0].high) || 0,
      low: parseFloat(priceResult.rows[0].low) || 0,
      volume: parseFloat(priceResult.rows[0].volume) || 0,
    } : null;

    // Fetch latest financial data
    const financialResult = await pool.query(
      `SELECT * FROM stock_financials 
       WHERE symbol = $1 
       ORDER BY period_end_date DESC 
       LIMIT 1`,
      [symbol]
    );
    const financial = financialResult.rows[0] ? (() => {
      const row = financialResult.rows[0];
      // Convert all numeric fields to numbers
      const convert = (val: any) => val != null ? (typeof val === 'string' ? parseFloat(val) : val) : null;
      return {
        ...row,
        peRatio: convert(row.pe_ratio),
        pegRatio: convert(row.peg_ratio),
        priceToBook: convert(row.price_to_book),
        priceToSales: convert(row.price_to_sales),
        evToEbitda: convert(row.ev_to_ebitda),
        epsTtm: convert(row.eps_ttm),
        epsGrowthYoy: convert(row.eps_growth_yoy),
        epsGrowthQoq: convert(row.eps_growth_qoq),
        revenue: convert(row.revenue),
        revenueGrowthYoy: convert(row.revenue_growth_yoy),
        revenueGrowthQoq: convert(row.revenue_growth_qoq),
        netIncome: convert(row.net_income),
        netIncomeGrowthYoy: convert(row.net_income_growth_yoy),
        profitMargin: convert(row.profit_margin),
        totalCash: convert(row.total_cash),
        totalDebt: convert(row.total_debt),
        cashPerShare: convert(row.cash_per_share),
        debtToEquity: convert(row.debt_to_equity),
        currentRatio: convert(row.current_ratio),
        roe: convert(row.roe),
        roic: convert(row.roic),
        roa: convert(row.roa),
        grossMargin: convert(row.gross_margin),
        operatingMargin: convert(row.operating_margin),
        revenueGrowth3y: convert(row.revenue_growth_3y),
        earningsGrowth3y: convert(row.earnings_growth_3y),
        sharesOutstanding: convert(row.shares_outstanding),
        floatShares: convert(row.float_shares),
      };
    })() : null;

    // Fetch latest score
    const scoreResult = await pool.query(
      `SELECT * FROM stock_scores 
       WHERE symbol = $1 
       ORDER BY calculated_at DESC 
       LIMIT 1`,
      [symbol]
    );
    const score = scoreResult.rows[0] ? (() => {
      const row = scoreResult.rows[0];
      const convert = (val: any) => val != null ? (typeof val === 'string' ? parseFloat(val) : val) : null;
      return {
        ...row,
        valuationScore: convert(row.valuation_score),
        healthScore: convert(row.health_score),
        growthScore: convert(row.growth_score),
        riskScore: convert(row.risk_score),
        overallScore: convert(row.overall_score),
        peScore: convert(row.pe_score),
        pegScore: convert(row.peg_score),
        debtScore: convert(row.debt_score),
        profitabilityScore: convert(row.profitability_score),
        growthRateScore: convert(row.growth_rate_score),
        volatilityScore: convert(row.volatility_score),
      };
    })() : null;

    // Fetch latest technical indicator (RSI)
    const technicalResult = await pool.query(
      `SELECT * FROM technical_indicators 
       WHERE symbol = $1 
       ORDER BY date DESC 
       LIMIT 1`,
      [symbol]
    );
    const technicalIndicator = technicalResult.rows[0] ? {
      ...technicalResult.rows[0],
      rsi: technicalResult.rows[0].rsi ? parseFloat(technicalResult.rows[0].rsi) : null,
    } : null;

    // Fetch historical prices (last 30 days)
    const historicalResult = await pool.query(
      `SELECT * FROM stock_prices 
       WHERE symbol = $1 
       ORDER BY date DESC 
       LIMIT 30`,
      [symbol]
    );
    const historicalPrices = historicalResult.rows.reverse(); // Oldest first for charts

    // Calculate recommendation
    const recommendation = calculateRecommendation(score, financial);

    const data: StockDetailData = {
      stock,
      latestPrice,
      financial,
      score,
      technicalIndicator,
      historicalPrices,
      recommendation,
    };

    return NextResponse.json(data);
  } catch (error) {
    console.error('Error fetching stock data:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}

