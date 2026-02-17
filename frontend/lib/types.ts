// Stock data types matching database schema

export interface Stock {
  symbol: string;
  name: string;
  exchange: string;
  sector: string | null;
  industry: string | null;
  marketCap: number | null;
  isActive: boolean;
  addedAt: string;
  updatedAt: string;
}

export interface StockPrice {
  symbol: string;
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  adjustedClose: number | null;
  volume: number;
  createdAt: string;
  updatedAt: string;
}

export interface StockFinancial {
  id: string;
  symbol: string;
  periodType: string;
  periodEndDate: string;
  fiscalYear: number | null;
  fiscalQuarter: number | null;
  peRatio: number | null;
  pegRatio: number | null;
  priceToBook: number | null;
  priceToSales: number | null;
  evToEbitda: number | null;
  epsTtm: number | null;
  epsGrowthYoy: number | null;
  epsGrowthQoq: number | null;
  revenue: number | null;
  revenueGrowthYoy: number | null;
  revenueGrowthQoq: number | null;
  netIncome: number | null;
  netIncomeGrowthYoy: number | null;
  profitMargin: number | null;
  totalCash: number | null;
  totalDebt: number | null;
  cashPerShare: number | null;
  debtToEquity: number | null;
  currentRatio: number | null;
  roe: number | null;
  roic: number | null;
  roa: number | null;
  grossMargin: number | null;
  operatingMargin: number | null;
  revenueGrowth3y: number | null;
  earningsGrowth3y: number | null;
  sharesOutstanding: number | null;
  floatShares: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface StockScore {
  id: string;
  symbol: string;
  calculatedAt: string;
  valuationCategory: string;
  valuationScore: number;
  healthScore: number;
  healthGrade: string;
  growthScore: number;
  growthGrade: string;
  riskScore: number;
  riskGrade: string;
  overallScore: number;
  overallGrade: string;
  peScore: number | null;
  pegScore: number | null;
  debtScore: number | null;
  profitabilityScore: number | null;
  growthRateScore: number | null;
  volatilityScore: number | null;
}

export interface TechnicalIndicator {
  id: string;
  symbol: string;
  date: string;
  rsi: number | null;
  createdAt: string;
  updatedAt: string;
}

export type Recommendation = 'BUY' | 'HOLD' | 'SELL' | 'AVOID';

export interface RecommendationData {
  recommendation: Recommendation;
  confidence: 'low' | 'medium' | 'high';
  overallScore: number;
  reasons: {
    valuation: string;
    financialStrength: string;
    growthOutlook: string;
    riskLevel: string;
  };
}

export interface StockDetailData {
  stock: Stock;
  latestPrice: StockPrice | null;
  financial: StockFinancial | null;
  score: StockScore | null;
  technicalIndicator: TechnicalIndicator | null;
  historicalPrices: StockPrice[];
  recommendation: RecommendationData;
}

