'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { TrendingUp, TrendingDown, DollarSign, AlertCircle } from 'lucide-react';
import SearchBar from '@/components/SearchBar';

interface StockListItem {
  symbol: string;
  name: string;
  sector: string | null;
  exchange: string | null;
  currentPrice: number | null;
  priceDate: string | null;
  overallScore: number;
  overallGrade: string | null;
  recommendation: string;
  confidence: string;
  peRatio: number | null;
  revenueGrowth: number | null;
}

export default function StocksPage() {
  const [activeTab, setActiveTab] = useState<'BUY' | 'HOLD' | 'SELL' | 'AVOID'>('BUY');
  const [stocks, setStocks] = useState<StockListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabCounts, setTabCounts] = useState<{ BUY: number; HOLD: number; SELL: number; AVOID: number }>({
    BUY: 0,
    HOLD: 0,
    SELL: 0,
    AVOID: 0,
  });

  // Fetch counts for all tabs on mount
  useEffect(() => {
    async function fetchAllCounts() {
      try {
        const types: Array<'BUY' | 'HOLD' | 'SELL' | 'AVOID'> = ['BUY', 'HOLD', 'SELL', 'AVOID'];
        const countPromises = types.map(async (type) => {
          const response = await fetch(`/api/stocks/by-recommendation/${type}`);
          if (response.ok) {
            const data = await response.json();
            return { type, count: data.count || (data.stocks?.length || 0) };
          }
          return { type, count: 0 };
        });
        
        const results = await Promise.all(countPromises);
        const counts = {
          BUY: 0,
          HOLD: 0,
          SELL: 0,
          AVOID: 0,
        };
        
        results.forEach(({ type, count }) => {
          counts[type] = count;
        });
        
        setTabCounts(counts);
      } catch (err) {
        console.error('Error fetching tab counts:', err);
      }
    }
    fetchAllCounts();
  }, []);

  useEffect(() => {
    async function fetchStocks() {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(`/api/stocks/by-recommendation/${activeTab}`);
        if (!response.ok) {
          throw new Error('Failed to fetch stocks');
        }
        const data = await response.json();
        setStocks(data.stocks || []);
        // Update the count for the active tab
        setTabCounts(prev => ({
          ...prev,
          [activeTab]: data.count || (data.stocks?.length || 0)
        }));
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load stocks');
        setStocks([]);
      } finally {
        setLoading(false);
      }
    }
    fetchStocks();
  }, [activeTab]);

  const formatNumber = (value: number | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    return value.toFixed(2);
  };

  const formatPercent = (value: number | null | undefined) => {
    if (value === null || value === undefined) return 'N/A';
    const sign = value > 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  };

  const getGradeColor = (grade: string | null) => {
    if (!grade) return 'rgba(255, 255, 255, 0.1)';
    const g = grade.toUpperCase();
    if (g === 'A') return 'rgba(16, 185, 129, 0.3)';
    if (g === 'B') return 'rgba(234, 179, 8, 0.3)';
    if (g === 'C') return 'rgba(249, 115, 22, 0.3)';
    return 'rgba(239, 68, 68, 0.3)';
  };

  const getGradeTextColor = (grade: string | null) => {
    if (!grade) return '#a3a3a3';
    const g = grade.toUpperCase();
    if (g === 'A') return '#10b981';
    if (g === 'B') return '#eab308';
    if (g === 'C') return '#f97316';
    return '#ef4444';
  };

  const tabs = [
    { id: 'BUY' as const, label: 'Buy', color: '#10b981' },
    { id: 'HOLD' as const, label: 'Hold', color: '#eab308' },
    { id: 'SELL' as const, label: 'Sell', color: '#f97316' },
    { id: 'AVOID' as const, label: 'Avoid', color: '#ef4444' },
  ];

  return (
    <>
      <div className="gradient-bg"></div>
      <div className="grid-overlay"></div>

      <nav>
        <div className="logo">
          <div className="logo-icon">T</div>
          <Link href="/" style={{ color: '#fafafa', textDecoration: 'none' }}>TrueEquity</Link>
        </div>
        <ul className="nav-links">
          <li><Link href="/">Home</Link></li>
          <li><Link href="/stocks">Stocks</Link></li>
          <li><Link href="/pricing">Pricing</Link></li>
          <li><Link href="/#faq">FAQ</Link></li>
        </ul>
      </nav>

      <main style={{ paddingTop: '100px', paddingBottom: '4rem', minHeight: '100vh' }}>
        <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '0 2rem' }}>
          {/* Search Bar */}
          <div style={{ marginBottom: '2rem' }}>
            <div className="search-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
              <SearchBar />
            </div>
          </div>

          {/* Header */}
          <div style={{ marginBottom: '2rem' }}>
            <h1 style={{ fontSize: '2.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
              Stock Recommendations
            </h1>
            <p style={{ color: '#a3a3a3', fontSize: '1.125rem' }}>
              Browse stocks by recommendation type. All data is preloaded and updated regularly.
            </p>
          </div>

          {/* Tabs */}
          <div className="tabs-container" style={{ 
            display: 'flex', 
            gap: '0.5rem', 
            marginBottom: '2rem',
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
            flexWrap: 'nowrap',
            overflowX: 'auto',
            WebkitOverflowScrolling: 'touch',
            scrollbarWidth: 'none',
            msOverflowStyle: 'none'
          }}>
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  padding: '0.75rem 1rem',
                  fontWeight: 500,
                  fontSize: '0.9rem',
                  transition: 'all 0.3s',
                  background: 'transparent',
                  border: 'none',
                  color: activeTab === tab.id ? tab.color : '#a3a3a3',
                  borderBottom: activeTab === tab.id ? `3px solid ${tab.color}` : '3px solid transparent',
                  cursor: 'pointer',
                  position: 'relative',
                  bottom: '-1px',
                  whiteSpace: 'nowrap',
                  flexShrink: 0
                }}
                onMouseEnter={(e) => {
                  if (activeTab !== tab.id) e.currentTarget.style.color = '#d1d5db';
                }}
                onMouseLeave={(e) => {
                  if (activeTab !== tab.id) e.currentTarget.style.color = '#a3a3a3';
                }}
              >
                {tab.label}
                <span style={{
                  marginLeft: '0.5rem',
                  padding: '0.125rem 0.5rem',
                  borderRadius: '12px',
                  background: activeTab === tab.id ? `${tab.color}20` : 'rgba(255, 255, 255, 0.1)',
                  color: activeTab === tab.id ? tab.color : '#a3a3a3',
                  fontSize: '0.875rem',
                  fontWeight: 600
                }}>
                  {tabCounts[tab.id]}
                </span>
              </button>
            ))}
          </div>

          {/* Content */}
          {loading ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#a3a3a3' }}>
              <div style={{ fontSize: '1.25rem' }}>Loading stocks...</div>
            </div>
          ) : error ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#ef4444' }}>
              <AlertCircle style={{ width: '48px', height: '48px', margin: '0 auto 1rem', color: '#ef4444' }} />
              <div style={{ fontSize: '1.25rem' }}>Error: {error}</div>
            </div>
          ) : stocks.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#a3a3a3' }}>
              <div style={{ fontSize: '1.25rem' }}>No stocks found with {activeTab} recommendation</div>
              <p style={{ marginTop: '0.5rem', color: '#666' }}>Stocks are being analyzed. Check back soon.</p>
            </div>
          ) : (
            <div className="stock-grid" style={{ 
              display: 'grid', 
              gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', 
              gap: '1.5rem' 
            }}>
              {stocks.map((stock) => (
                <Link
                  key={stock.symbol}
                  href={`/stock/${stock.symbol}`}
                  style={{ textDecoration: 'none' }}
                  className="stock-card-link"
                >
                  <div className="prop-card stock-card" style={{ 
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    height: '100%'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-4px)';
                    e.currentTarget.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.3)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = 'none';
                  }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
                      <div>
                        <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fafafa', marginBottom: '0.25rem' }}>
                          {stock.symbol}
                        </div>
                        <div style={{ fontSize: '0.875rem', color: '#a3a3a3', marginBottom: '0.5rem' }}>
                          {stock.name}
                        </div>
                        {stock.sector && (
                          <div style={{ 
                            fontSize: '0.75rem', 
                            padding: '0.25rem 0.5rem', 
                            background: 'rgba(255, 255, 255, 0.05)', 
                            borderRadius: '4px',
                            display: 'inline-block',
                            color: '#a3a3a3'
                          }}>
                            {stock.sector}
                          </div>
                        )}
                      </div>
                      <div style={{
                        padding: '0.5rem 0.75rem',
                        borderRadius: '6px',
                        fontWeight: 700,
                        fontSize: '0.875rem',
                        background: activeTab === 'BUY' ? 'rgba(16, 185, 129, 0.3)' :
                                   activeTab === 'HOLD' ? 'rgba(234, 179, 8, 0.3)' :
                                   activeTab === 'SELL' ? 'rgba(249, 115, 22, 0.3)' :
                                   'rgba(239, 68, 68, 0.3)',
                        color: activeTab === 'BUY' ? '#10b981' :
                               activeTab === 'HOLD' ? '#eab308' :
                               activeTab === 'SELL' ? '#f97316' :
                               '#ef4444',
                        border: '1px solid',
                        borderColor: activeTab === 'BUY' ? 'rgba(16, 185, 129, 0.5)' :
                                   activeTab === 'HOLD' ? 'rgba(234, 179, 8, 0.5)' :
                                   activeTab === 'SELL' ? 'rgba(249, 115, 22, 0.5)' :
                                   'rgba(239, 68, 68, 0.5)'
                      }}>
                        {stock.recommendation}
                      </div>
                    </div>

                    <div style={{ 
                      display: 'grid', 
                      gridTemplateColumns: '1fr 1fr', 
                      gap: '1rem',
                      marginTop: '1rem',
                      paddingTop: '1rem',
                      borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                    }}>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#a3a3a3', marginBottom: '0.25rem' }}>Current Price</div>
                        <div style={{ fontSize: '1.125rem', fontWeight: 700, color: '#fafafa' }}>
                          {stock.currentPrice ? `$${formatNumber(stock.currentPrice)}` : 'N/A'}
                        </div>
                      </div>
                      <div>
                        <div style={{ fontSize: '0.75rem', color: '#a3a3a3', marginBottom: '0.25rem' }}>Overall Score</div>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                          <div style={{ fontSize: '1.125rem', fontWeight: 700, color: '#fafafa' }}>
                            {formatNumber(stock.overallScore)}/100
                          </div>
                          {stock.overallGrade && (
                            <span style={{
                              fontSize: '0.75rem',
                              padding: '0.125rem 0.375rem',
                              borderRadius: '4px',
                              background: getGradeColor(stock.overallGrade),
                              color: getGradeTextColor(stock.overallGrade),
                              fontWeight: 700
                            }}>
                              {stock.overallGrade}
                            </span>
                          )}
                        </div>
                      </div>
                      {stock.peRatio && (
                        <div>
                          <div style={{ fontSize: '0.75rem', color: '#a3a3a3', marginBottom: '0.25rem' }}>P/E Ratio</div>
                          <div style={{ fontSize: '1rem', fontWeight: 600, color: '#fafafa' }}>
                            {formatNumber(stock.peRatio)}
                          </div>
                        </div>
                      )}
                      {stock.revenueGrowth !== null && (
                        <div>
                          <div style={{ fontSize: '0.75rem', color: '#a3a3a3', marginBottom: '0.25rem' }}>Revenue Growth</div>
                          <div style={{ 
                            fontSize: '1rem', 
                            fontWeight: 600, 
                            color: stock.revenueGrowth >= 0 ? '#10b981' : '#ef4444' 
                          }}>
                            {formatPercent(stock.revenueGrowth)}
                          </div>
                        </div>
                      )}
                    </div>

                    <div style={{ 
                      marginTop: '1rem',
                      paddingTop: '1rem',
                      borderTop: '1px solid rgba(255, 255, 255, 0.1)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div style={{ fontSize: '0.75rem', color: '#a3a3a3' }}>
                        Confidence: <span style={{ 
                          color: stock.confidence === 'high' ? '#10b981' : 
                                 stock.confidence === 'medium' ? '#eab308' : 
                                 '#f97316',
                          fontWeight: 600
                        }}>
                          {stock.confidence.toUpperCase()}
                        </span>
                      </div>
                      <div style={{ 
                        fontSize: '0.75rem', 
                        color: '#60a5fa',
                        fontWeight: 500
                      }}>
                        View Details →
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </main>
    </>
  );
}
