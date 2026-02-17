'use client';

import { useState, useEffect } from 'react';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import { StockDetailData } from '@/lib/types';
import { TrendingUp, TrendingDown, DollarSign, Activity, BarChart3, Shield, AlertCircle } from 'lucide-react';
import SearchBar from '@/components/SearchBar';

/**
 * Fetches comprehensive stock data from the API.
 * 
 * @param symbol - Stock symbol (e.g., "AAPL", "MSFT")
 * @returns Promise resolving to StockDetailData or null if not found
 */
async function getStockData(symbol: string): Promise<StockDetailData | null> {
  try {
    const baseUrl = process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';
    const response = await fetch(`${baseUrl}/api/stocks/${symbol}`, {
      next: { revalidate: 60 },
    });

    if (!response.ok) {
      return null;
    }

    return await response.json();
  } catch (error) {
    console.error('Error fetching stock data:', error);
    return null;
  }
}

export default function StockDetailPage({
  params,
}: {
  params: Promise<{ symbol: string }>;
}) {
  const [activeTab, setActiveTab] = useState('overview');
  const [data, setData] = useState<StockDetailData | null>(null);
  const [loading, setLoading] = useState(true);
  const [symbol, setSymbol] = useState<string>('');
  const [rsiTimeframe, setRsiTimeframe] = useState<string>('1d'); // Default to daily
  const [rsiValue, setRsiValue] = useState<number | null>(null);
  const [rsiLoading, setRsiLoading] = useState(false);

  useEffect(() => {
    async function fetchData() {
      const resolvedParams = await params;
      const symbolParam = resolvedParams.symbol.toUpperCase();
      setSymbol(symbolParam);
      const stockData = await getStockData(symbolParam);
      setData(stockData);
      setLoading(false);
    }
    fetchData();
  }, [params]);

  // Fetch RSI for selected timeframe
  useEffect(() => {
    async function fetchRSI() {
      if (!symbol) return;
      
      setRsiLoading(true);
      try {
        const baseUrl = process.env.NEXT_PUBLIC_APP_URL || 'http://localhost:3000';
        const response = await fetch(`${baseUrl}/api/stocks/${symbol}/rsi?timeframe=${rsiTimeframe}`);
        
        if (response.ok) {
          const data = await response.json();
          setRsiValue(data.rsi);
        } else {
          setRsiValue(null);
        }
      } catch (error) {
        console.error('Error fetching RSI:', error);
        setRsiValue(null);
      } finally {
        setRsiLoading(false);
      }
    }
    
    fetchRSI();
  }, [symbol, rsiTimeframe]);

  if (loading) {
    return (
      <div className="min-h-screen bg-[#0f1419] text-white flex items-center justify-center">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  if (!data) {
    notFound();
  }

  const { stock, latestPrice, financial, score, technicalIndicator, historicalPrices, recommendation } = data;

  // Calculate price change
  let priceChange = 0;
  let priceChangePercent = 0;
  if (historicalPrices.length >= 2) {
    const firstPrice = historicalPrices[0].close;
    const lastPrice = historicalPrices[historicalPrices.length - 1].close;
    priceChange = lastPrice - firstPrice;
    priceChangePercent = firstPrice !== 0 ? (priceChange / firstPrice) * 100 : 0;
  }

  const currentPrice = latestPrice ? Number(latestPrice.close) : 0;

  // Format helpers
  const formatNumber = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return numValue.toFixed(2);
  };

  const formatPercent = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    return `${numValue.toFixed(2)}%`;
  };

  const formatCurrency = (value: number | string | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numValue)) return 'N/A';
    if (numValue >= 1e9) return `$${(numValue / 1e9).toFixed(2)}B`;
    if (numValue >= 1e6) return `$${(numValue / 1e6).toFixed(2)}M`;
    return `$${numValue.toFixed(2)}`;
  };

  // Get RSI signal
  const getRSISignal = (rsi: number | null) => {
    if (rsi === null || rsi === undefined) return 'N/A';
    if (rsi < 30) return 'Oversold';
    if (rsi > 70) return 'Overbought';
    return 'Neutral';
  };

  return (
    <>
      <div className="gradient-bg"></div>
      <div className="grid-overlay"></div>

      <nav>
        <div className="logo">
          <div className="logo-icon">T</div>
          <Link href="/">TrueEquity</Link>
        </div>
        <ul className="nav-links">
          <li><Link href="/">Home</Link></li>
          <li><Link href="/stocks">Stocks</Link></li>
          <li><Link href="/pricing">Pricing</Link></li>
          <li><Link href="/#faq">FAQ</Link></li>
        </ul>
      </nav>

      <div style={{ paddingTop: '100px', paddingBottom: '4rem', minHeight: '100vh' }}>
        {/* Search Bar - Positioned above stock name with high z-index */}
        <div style={{ 
          maxWidth: '1280px', 
          margin: '0 auto', 
          padding: '0 2rem', 
          marginBottom: '1.5rem',
          position: 'relative',
          zIndex: 1000
        }}>
          <div className="search-container" style={{ maxWidth: '600px', margin: '0 auto', position: 'relative', zIndex: 1000 }}>
            <SearchBar />
          </div>
        </div>

        {/* Header */}
        <div style={{ 
          maxWidth: '1280px', 
          margin: '0 auto', 
          padding: '0 2rem', 
          marginBottom: '2rem',
          position: 'relative',
          zIndex: 1
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <div>
              <h1 style={{ fontSize: '2.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                {stock.name}
              </h1>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#a3a3a3' }}>
                <span style={{ fontSize: '0.875rem' }}>{stock.symbol}</span>
                {stock.sector && (
                  <span style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', background: 'rgba(255, 255, 255, 0.05)', borderRadius: '4px' }}>
                    {stock.sector}
                  </span>
                )}
              </div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '2rem', fontWeight: 700, color: '#fafafa' }}>
                ${formatNumber(currentPrice)}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', justifyContent: 'flex-end', color: priceChange >= 0 ? '#10b981' : '#ef4444' }}>
                {priceChange >= 0 ? <TrendingUp style={{ width: '16px', height: '16px' }} /> : <TrendingDown style={{ width: '16px', height: '16px' }} />}
                <span style={{ fontWeight: 600 }}>
                  {priceChange >= 0 ? '+' : ''}${formatNumber(priceChange)} ({formatNumber(priceChangePercent)}%)
                </span>
              </div>
            </div>
          </div>

          {/* Score Banner */}
          <div style={{
            background: 'linear-gradient(to right, rgba(161, 98, 7, 0.3), rgba(154, 52, 18, 0.3))',
            border: '1px solid rgba(161, 98, 7, 0.5)',
            borderRadius: '8px',
            padding: '1rem',
            marginTop: '1rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <AlertCircle style={{ width: '24px', height: '24px', color: '#fbbf24' }} />
                <div>
                  <div style={{ fontSize: '0.875rem', color: '#d1d5db' }}>Overall Score</div>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(recommendation.overallScore)}/100
                    </div>
                    {score?.overallGrade && (
                      <span style={{
                        fontSize: '0.875rem',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        background: score.overallGrade === 'A' ? 'rgba(16, 185, 129, 0.3)' : 
                                   score.overallGrade === 'B' ? 'rgba(234, 179, 8, 0.3)' :
                                   score.overallGrade === 'C' ? 'rgba(249, 115, 22, 0.3)' :
                                   'rgba(239, 68, 68, 0.3)',
                        color: score.overallGrade === 'A' ? '#10b981' : 
                               score.overallGrade === 'B' ? '#eab308' :
                               score.overallGrade === 'C' ? '#f97316' :
                               '#ef4444',
                        fontWeight: 700,
                        border: '1px solid',
                        borderColor: score.overallGrade === 'A' ? 'rgba(16, 185, 129, 0.5)' : 
                                   score.overallGrade === 'B' ? 'rgba(234, 179, 8, 0.5)' :
                                   score.overallGrade === 'C' ? 'rgba(249, 115, 22, 0.5)' :
                                   'rgba(239, 68, 68, 0.5)'
                      }}>
                        Grade: {score.overallGrade}
                      </span>
                    )}
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', flexWrap: 'wrap' }}>
                {/* Recommendation Badge */}
                <div style={{
                  padding: '0.5rem 1.25rem',
                  borderRadius: '8px',
                  fontWeight: 700,
                  fontSize: '1.125rem',
                  background: recommendation.recommendation === 'BUY' ? 'rgba(16, 185, 129, 0.3)' :
                              recommendation.recommendation === 'HOLD' ? 'rgba(234, 179, 8, 0.3)' :
                              recommendation.recommendation === 'SELL' ? 'rgba(249, 115, 22, 0.3)' :
                              'rgba(239, 68, 68, 0.3)',
                  color: recommendation.recommendation === 'BUY' ? '#10b981' :
                         recommendation.recommendation === 'HOLD' ? '#eab308' :
                         recommendation.recommendation === 'SELL' ? '#f97316' :
                         '#ef4444',
                  border: '2px solid',
                  borderColor: recommendation.recommendation === 'BUY' ? 'rgba(16, 185, 129, 0.6)' :
                               recommendation.recommendation === 'HOLD' ? 'rgba(234, 179, 8, 0.6)' :
                               recommendation.recommendation === 'SELL' ? 'rgba(249, 115, 22, 0.6)' :
                               'rgba(239, 68, 68, 0.6)'
                }}>
                  {recommendation.recommendation}
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.875rem', color: '#a3a3a3' }}>Confidence Level</div>
                  <div style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fbbf24' }}>
                    {recommendation.confidence.toUpperCase()}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '0 2rem', marginBottom: '2rem' }}>
          <div className="tabs-container" style={{ 
            display: 'flex', 
            gap: '0.5rem', 
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
            overflowX: 'auto',
            WebkitOverflowScrolling: 'touch',
            scrollbarWidth: 'none',
            msOverflowStyle: 'none'
          }}>
            {['overview', 'financials', 'analysis'].map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                style={{
                  padding: '0.75rem 1rem',
                  fontWeight: 500,
                  textTransform: 'capitalize',
                  transition: 'all 0.3s',
                  background: 'transparent',
                  border: 'none',
                  color: activeTab === tab ? '#60a5fa' : '#a3a3a3',
                  borderBottom: activeTab === tab ? '2px solid #60a5fa' : '2px solid transparent',
                  cursor: 'pointer',
                  whiteSpace: 'nowrap',
                  flexShrink: 0,
                  fontSize: '0.9rem'
                }}
                onMouseEnter={(e) => {
                  if (activeTab !== tab) e.currentTarget.style.color = '#d1d5db';
                }}
                onMouseLeave={(e) => {
                  if (activeTab !== tab) e.currentTarget.style.color = '#a3a3a3';
                }}
              >
                {tab}
              </button>
            ))}
          </div>
        </div>

        {/* Main Content */}
        <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '0 2rem' }}>
          {activeTab === 'overview' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {/* Key Metrics Grid */}
              <div className="stock-detail-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <DollarSign style={{ width: '16px', height: '16px', color: '#a3a3a3' }} />
                      <span style={{ color: '#a3a3a3', fontSize: '0.875rem' }}>P/E Ratio</span>
                    </div>
                  </div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa', marginBottom: '0.25rem' }}>
                    {financial?.peRatio ? formatNumber(financial.peRatio) : 'N/A'}
                  </div>
                </div>

                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <BarChart3 style={{ width: '16px', height: '16px', color: '#a3a3a3' }} />
                      <span style={{ color: '#a3a3a3', fontSize: '0.875rem' }}>Price-to-Book</span>
                    </div>
                  </div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa', marginBottom: '0.25rem' }}>
                    {financial?.priceToBook ? formatNumber(financial.priceToBook) : 'N/A'}
                  </div>
                </div>

                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <TrendingUp style={{ width: '16px', height: '16px', color: '#a3a3a3' }} />
                      <span style={{ color: '#a3a3a3', fontSize: '0.875rem' }}>Revenue Growth</span>
                    </div>
                    {financial?.revenueGrowthYoy != null && (
                      <span style={{
                        fontSize: '0.75rem',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        background: financial.revenueGrowthYoy > 0 ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)',
                        color: financial.revenueGrowthYoy > 0 ? '#10b981' : '#ef4444'
                      }}>
                        {financial.revenueGrowthYoy > 0 ? '+' : ''}{formatNumber(financial.revenueGrowthYoy)}%
                      </span>
                    )}
                  </div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa', marginBottom: '0.25rem' }}>
                    {financial?.revenueGrowthYoy != null ? `${formatNumber(financial.revenueGrowthYoy)}%` : 'N/A'}
                  </div>
                </div>

                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'start', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Activity style={{ width: '16px', height: '16px', color: '#a3a3a3' }} />
                      <span style={{ color: '#a3a3a3', fontSize: '0.875rem' }}>Profit Margin</span>
                    </div>
                  </div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa', marginBottom: '0.25rem' }}>
                    {financial?.profitMargin != null ? `${formatNumber(financial.profitMargin)}%` : 'N/A'}
                  </div>
                </div>
              </div>

              {/* Financial Strength & Growth */}
              <div className="stock-detail-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '1.5rem' }}>
                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Shield style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                      <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Financial Strength</h3>
                    </div>
                    {score?.healthGrade && (
                      <span style={{
                        fontSize: '0.75rem',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        background: score.healthGrade === 'A' ? 'rgba(16, 185, 129, 0.3)' : 
                                   score.healthGrade === 'B' ? 'rgba(234, 179, 8, 0.3)' :
                                   score.healthGrade === 'C' ? 'rgba(249, 115, 22, 0.3)' :
                                   'rgba(239, 68, 68, 0.3)',
                        color: score.healthGrade === 'A' ? '#10b981' : 
                               score.healthGrade === 'B' ? '#eab308' :
                               score.healthGrade === 'C' ? '#f97316' :
                               '#ef4444',
                        fontWeight: 700,
                        border: '1px solid',
                        borderColor: score.healthGrade === 'A' ? 'rgba(16, 185, 129, 0.5)' : 
                                   score.healthGrade === 'B' ? 'rgba(234, 179, 8, 0.5)' :
                                   score.healthGrade === 'C' ? 'rgba(249, 115, 22, 0.5)' :
                                   'rgba(239, 68, 68, 0.5)'
                      }}>
                        Grade: {score.healthGrade}
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid rgba(255, 255, 255, 0.1)' }}>
                      <span style={{ color: '#a3a3a3' }}>Debt-to-Equity</span>
                      <span style={{ fontWeight: 600, color: '#fafafa' }}>
                        {financial?.debtToEquity != null ? formatNumber(financial.debtToEquity) : 'N/A'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid rgba(255, 255, 255, 0.1)' }}>
                      <span style={{ color: '#a3a3a3' }}>Current Ratio</span>
                      <span style={{ fontWeight: 600, color: '#fafafa' }}>
                        {financial?.currentRatio != null ? formatNumber(financial.currentRatio) : 'N/A'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0' }}>
                      <span style={{ color: '#a3a3a3' }}>ROE</span>
                      <span style={{ fontWeight: 600, color: '#fafafa' }}>
                        {financial?.roe != null ? `${formatNumber(financial.roe)}%` : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="prop-card">
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <TrendingUp style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                      <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Growth Metrics</h3>
                    </div>
                    {score?.growthGrade && (
                      <span style={{
                        fontSize: '0.75rem',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '4px',
                        background: score.growthGrade === 'A' ? 'rgba(16, 185, 129, 0.3)' : 
                                   score.growthGrade === 'B' ? 'rgba(234, 179, 8, 0.3)' :
                                   score.growthGrade === 'C' ? 'rgba(249, 115, 22, 0.3)' :
                                   'rgba(239, 68, 68, 0.3)',
                        color: score.growthGrade === 'A' ? '#10b981' : 
                               score.growthGrade === 'B' ? '#eab308' :
                               score.growthGrade === 'C' ? '#f97316' :
                               '#ef4444',
                        fontWeight: 700,
                        border: '1px solid',
                        borderColor: score.growthGrade === 'A' ? 'rgba(16, 185, 129, 0.5)' : 
                                   score.growthGrade === 'B' ? 'rgba(234, 179, 8, 0.5)' :
                                   score.growthGrade === 'C' ? 'rgba(249, 115, 22, 0.5)' :
                                   'rgba(239, 68, 68, 0.5)'
                      }}>
                        Grade: {score.growthGrade}
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid rgba(255, 255, 255, 0.1)' }}>
                      <span style={{ color: '#a3a3a3' }}>Revenue Growth</span>
                      <span style={{ fontWeight: 600, color: financial?.revenueGrowthYoy != null && financial.revenueGrowthYoy >= 0 ? '#10b981' : '#ef4444' }}>
                        {financial?.revenueGrowthYoy != null ? `${financial.revenueGrowthYoy >= 0 ? '+' : ''}${formatNumber(financial.revenueGrowthYoy)}%` : 'N/A'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid rgba(255, 255, 255, 0.1)' }}>
                      <span style={{ color: '#a3a3a3' }}>EPS Growth</span>
                      <span style={{ fontWeight: 600, color: financial?.epsGrowthYoy != null && financial.epsGrowthYoy >= 0 ? '#10b981' : '#ef4444' }}>
                        {financial?.epsGrowthYoy != null ? `${financial.epsGrowthYoy >= 0 ? '+' : ''}${formatNumber(financial.epsGrowthYoy)}%` : 'N/A'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0' }}>
                      <span style={{ color: '#a3a3a3' }}>Profit Margin</span>
                      <span style={{ fontWeight: 600, color: '#fafafa' }}>
                        {financial?.profitMargin != null ? `${formatNumber(financial.profitMargin)}%` : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'financials' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div className="prop-card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                  <DollarSign style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                  <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Revenue & Income</h3>
                </div>
                <div className="stock-detail-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
                  <div>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Total Revenue</div>
                    <div style={{ fontSize: '2rem', fontWeight: 700, color: '#10b981' }}>
                      {formatCurrency(financial?.revenue)}
                    </div>
                  </div>
                  <div>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Net Income</div>
                    <div style={{ fontSize: '2rem', fontWeight: 700, color: '#10b981' }}>
                      {formatCurrency(financial?.netIncome)}
                    </div>
                  </div>
                  <div>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Total Cash</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatCurrency(financial?.totalCash)}
                    </div>
                  </div>
                  <div>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Total Debt</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#ef4444' }}>
                      {formatCurrency(financial?.totalDebt)}
                    </div>
                  </div>
                </div>
              </div>

              <div className="prop-card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                  <Activity style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                  <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Profitability Ratios</h3>
                </div>
                <div className="stock-detail-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
                  <div style={{ textAlign: 'center', padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.75rem', marginBottom: '0.25rem' }}>EPS</div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(financial?.epsTtm)}
                    </div>
                  </div>
                  <div style={{ textAlign: 'center', padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.75rem', marginBottom: '0.25rem' }}>ROE</div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fafafa' }}>
                      {financial?.roe != null ? `${formatNumber(financial.roe)}%` : 'N/A'}
                    </div>
                  </div>
                  <div style={{ textAlign: 'center', padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.75rem', marginBottom: '0.25rem' }}>ROIC</div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fafafa' }}>
                      {financial?.roic != null ? `${formatNumber(financial.roic)}%` : 'N/A'}
                    </div>
                  </div>
                  <div style={{ textAlign: 'center', padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.75rem', marginBottom: '0.25rem' }}>Margin</div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fafafa' }}>
                      {financial?.profitMargin != null ? `${formatNumber(financial.profitMargin)}%` : 'N/A'}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'analysis' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div className="prop-card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                  <BarChart3 style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                  <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Valuation Analysis</h3>
                </div>
                <div className="stock-detail-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.5rem' }}>P/E Ratio</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(financial?.peRatio)}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>Price-to-Earnings</div>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.5rem' }}>PEG Ratio</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(financial?.pegRatio)}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>Price/Earnings to Growth</div>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.5rem' }}>P/B Ratio</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(financial?.priceToBook)}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>Price-to-Book</div>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255, 255, 255, 0.02)', borderRadius: '8px' }}>
                    <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.5rem' }}>P/S Ratio</div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#fafafa' }}>
                      {formatNumber(financial?.priceToSales)}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>Price-to-Sales</div>
                  </div>
                </div>
              </div>

              <div className="prop-card">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                  <AlertCircle style={{ width: '20px', height: '20px', color: '#60a5fa' }} />
                  <h3 style={{ fontSize: '1.125rem', fontWeight: 600, color: '#fafafa' }}>Risk Analysis</h3>
                </div>
                
                {/* Timeframe Selector Buttons */}
                <div className="rsi-buttons" style={{
                  display: 'flex',
                  gap: '0.5rem', 
                  marginBottom: '1rem',
                  flexWrap: 'wrap'
                }}>
                  <button
                    onClick={() => setRsiTimeframe('1h')}
                    style={{
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      background: rsiTimeframe === '1h' ? 'rgba(96, 165, 250, 0.3)' : 'rgba(255, 255, 255, 0.05)',
                      color: '#fafafa',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: rsiTimeframe === '1h' ? 600 : 400,
                      transition: 'all 0.2s'
                    }}
                  >
                    1 Hour
                  </button>
                  <button
                    onClick={() => setRsiTimeframe('30m')}
                    style={{
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      background: rsiTimeframe === '30m' ? 'rgba(96, 165, 250, 0.3)' : 'rgba(255, 255, 255, 0.05)',
                      color: '#fafafa',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: rsiTimeframe === '30m' ? 600 : 400,
                      transition: 'all 0.2s'
                    }}
                  >
                    1 Month
                  </button>
                  <button
                    onClick={() => setRsiTimeframe('2h')}
                    style={{
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      background: rsiTimeframe === '2h' ? 'rgba(96, 165, 250, 0.3)' : 'rgba(255, 255, 255, 0.05)',
                      color: '#fafafa',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: rsiTimeframe === '2h' ? 600 : 400,
                      transition: 'all 0.2s'
                    }}
                  >
                    6 Months
                  </button>
                  <button
                    onClick={() => setRsiTimeframe('1d')}
                    style={{
                      padding: '0.5rem 1rem',
                      borderRadius: '6px',
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      background: rsiTimeframe === '1d' ? 'rgba(96, 165, 250, 0.3)' : 'rgba(255, 255, 255, 0.05)',
                      color: '#fafafa',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: rsiTimeframe === '1d' ? 600 : 400,
                      transition: 'all 0.2s'
                    }}
                  >
                    1 Year
                  </button>
                </div>

                <div style={{
                  background: 'rgba(239, 68, 68, 0.2)',
                  border: '1px solid rgba(239, 68, 68, 0.5)',
                  borderRadius: '8px',
                  padding: '1.5rem'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div>
                      <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
                        RSI Signal (14-period) - {rsiTimeframe === '1h' ? '1 Hour' : rsiTimeframe === '30m' ? '1 Month' : rsiTimeframe === '2h' ? '6 Months' : '1 Year'}
                      </div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#ef4444' }}>
                        {rsiLoading ? 'Loading...' : (rsiValue !== null ? getRSISignal(rsiValue) : 'N/A')}
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ color: '#a3a3a3', fontSize: '0.875rem', marginBottom: '0.25rem' }}>RSI Value</div>
                      <div style={{ fontSize: '2rem', fontWeight: 700, color: '#fafafa' }}>
                        {rsiLoading ? '...' : (rsiValue !== null ? formatNumber(rsiValue) : 'N/A')}
                      </div>
                    </div>
                  </div>
                  <div style={{ marginTop: '1rem', fontSize: '0.875rem', color: '#a3a3a3' }}>
                    RSI below 30 typically indicates oversold conditions, which may present a buying opportunity.
                    {rsiTimeframe === '1h' && ' (Matches TradingView 1-hour chart)'}
                    {rsiTimeframe === '30m' && ' (Matches TradingView 1-month view)'}
                    {rsiTimeframe === '2h' && ' (Matches TradingView 6-month view)'}
                    {rsiTimeframe === '1d' && ' (Matches TradingView 1-year view)'}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div style={{ maxWidth: '1280px', margin: '2rem auto 0', padding: '1.5rem 2rem 0', borderTop: '1px solid rgba(255, 255, 255, 0.1)' }}>
          <p style={{ fontSize: '0.75rem', color: '#666', textAlign: 'center' }}>
            This is not financial advice. TrueEquity provides decision-support information only. 
            All investment decisions are your own responsibility. Past performance does not guarantee future results.
          </p>
        </div>
      </div>
    </>
  );
}
