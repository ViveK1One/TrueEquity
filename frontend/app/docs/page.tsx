import Link from 'next/link';

export default function DocumentationPage() {
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
            Documentation
          </h1>
          <p style={{ fontSize: '1.25rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '4rem' }}>
            Learn how to use TrueEquity to make informed investment decisions
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div className="prop-card">
              <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                Getting Started
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                TrueEquity is a stock analysis platform that provides data-driven insights to help you make informed investment decisions. 
                Our platform analyzes stocks based on fundamental metrics, growth indicators, and risk factors.
              </p>
              <h3 style={{ fontSize: '1.5rem', fontWeight: 600, marginTop: '1.5rem', marginBottom: '0.5rem', color: '#fafafa' }}>
                How to Search for Stocks
              </h3>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Use the search bar on the home page to find stocks by symbol (e.g., AAPL, MSFT) or company name. 
                Click on any result to view detailed analysis.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                Understanding Stock Scores
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                Each stock receives scores in four key categories:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem', marginBottom: '1rem' }}>
                <li><strong>Valuation Score (25% weight):</strong> Based on P/E ratio, PEG ratio, and Price-to-Book ratio</li>
                <li><strong>Financial Health Score (30% weight):</strong> Based on Debt-to-Equity and Current Ratio</li>
                <li><strong>Growth Score (30% weight):</strong> Based on Revenue Growth and EPS Growth</li>
                <li><strong>Risk Score (15% weight, inverted):</strong> Based on debt levels and liquidity</li>
              </ul>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                The overall score is a weighted average of these categories, converted to a letter grade (A, B, C, D, F).
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                Recommendations Explained
              </h2>
              <div className="features-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
                <div>
                  <h4 style={{ color: '#10b981', fontWeight: 600, marginBottom: '0.5rem' }}>BUY</h4>
                  <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>Overall score ≥ 70. Strong fundamentals and growth potential.</p>
                </div>
                <div>
                  <h4 style={{ color: '#eab308', fontWeight: 600, marginBottom: '0.5rem' }}>HOLD</h4>
                  <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>Overall score 50-69. Moderate performance, maintain position.</p>
                </div>
                <div>
                  <h4 style={{ color: '#f97316', fontWeight: 600, marginBottom: '0.5rem' }}>SELL</h4>
                  <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>Overall score 30-49. Weak fundamentals, consider selling.</p>
                </div>
                <div>
                  <h4 style={{ color: '#ef4444', fontWeight: 600, marginBottom: '0.5rem' }}>AVOID</h4>
                  <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>Overall score &lt; 30. High risk, avoid investment.</p>
                </div>
              </div>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                RSI (Relative Strength Index)
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                RSI is a momentum indicator that measures the speed and magnitude of price changes. Our RSI calculations match TradingView's values for consistency.
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li><strong>RSI &gt; 70:</strong> Overbought (potential sell signal)</li>
                <li><strong>RSI 30-70:</strong> Neutral</li>
                <li><strong>RSI &lt; 30:</strong> Oversold (potential buy signal)</li>
              </ul>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginTop: '1rem' }}>
                You can view RSI for different timeframes: 1 hour, 1 month, 6 months, and 1 year.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                Browsing Stocks by Recommendation
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Visit the <Link href="/stocks" style={{ color: '#60a5fa', textDecoration: 'underline' }}>Stocks</Link> page to browse stocks filtered by recommendation type. 
                All stocks are pre-loaded and updated regularly with the latest analysis.
              </p>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
