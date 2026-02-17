import Link from 'next/link';

export default function FeaturesPage() {
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
        <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 2rem' }}>
          <h1 style={{ fontSize: '3rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa', textAlign: 'center' }}>
            Features
          </h1>
          <p style={{ fontSize: '1.25rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '4rem' }}>
            Comprehensive stock analysis tools for informed investment decisions
          </p>

          <div className="features-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem', marginBottom: '4rem' }}>
            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>📊</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Comprehensive Analysis
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Get detailed financial metrics including P/E ratios, PEG ratios, revenue growth, EPS growth, and more. All data is pre-computed for instant access.
              </p>
            </div>

            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>🎯</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Clear Recommendations
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Receive clear BUY, HOLD, SELL, or AVOID recommendations based on comprehensive scoring of valuation, financial health, growth, and risk factors.
              </p>
            </div>

            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>📈</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Technical Indicators
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Access RSI (Relative Strength Index) calculations for multiple timeframes (1 hour, 1 month, 6 months, 1 year) matching TradingView's values.
              </p>
            </div>

            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>🔍</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Real-Time Search
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Instantly search for stocks by symbol or company name. Get instant results with debounced search for optimal performance.
              </p>
            </div>

            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>📱</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Fully Responsive
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Access TrueEquity from any device - desktop, tablet, or mobile. All features work seamlessly across all screen sizes.
              </p>
            </div>

            <div className="prop-card">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>⚡</div>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#fafafa' }}>
                Pre-Computed Scores
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                All scores are calculated and stored in advance, ensuring fast page loads and instant access to analysis data.
              </p>
            </div>
          </div>

          <div className="prop-card" style={{ marginTop: '2rem', textAlign: 'center' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Ready to Get Started?
            </h2>
            <p style={{ color: '#a3a3a3', marginBottom: '2rem' }}>
              Explore our pricing plans and start analyzing stocks today.
            </p>
            <Link href="/pricing" style={{ 
              display: 'inline-block',
              padding: '0.75rem 2rem',
              background: '#10b981',
              color: '#fff',
              borderRadius: '8px',
              textDecoration: 'none',
              fontWeight: 600,
              transition: 'all 0.3s'
            }}>
              View Pricing
            </Link>
          </div>
        </div>
      </main>
    </>
  );
}
