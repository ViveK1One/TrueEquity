import Link from 'next/link';

export default function AboutPage() {
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
            About TrueEquity
          </h1>
          <p style={{ fontSize: '1.25rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '4rem' }}>
            Data-driven stock analysis for informed investment decisions
          </p>

          <div className="prop-card" style={{ marginBottom: '2rem' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Our Mission
            </h2>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
              TrueEquity was created to provide honest, data-driven stock analysis without the hype and predictions 
              that often cloud investment decisions. We believe that investors deserve transparent, educational insights 
              based on fundamental analysis, growth metrics, and risk assessment.
            </p>
          </div>

          <div className="prop-card" style={{ marginBottom: '2rem' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              What We Do
            </h2>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
              TrueEquity analyzes stocks using a comprehensive scoring system that evaluates:
            </p>
            <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
              <li>Valuation metrics (P/E, PEG, Price-to-Book ratios)</li>
              <li>Financial health (debt levels, liquidity ratios)</li>
              <li>Growth indicators (revenue growth, earnings growth)</li>
              <li>Risk factors (volatility, financial stability)</li>
              <li>Technical indicators (RSI for multiple timeframes)</li>
            </ul>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginTop: '1rem' }}>
              Based on these analyses, we provide clear BUY, HOLD, SELL, or AVOID recommendations with confidence levels.
            </p>
          </div>

          <div className="prop-card" style={{ marginBottom: '2rem' }}>
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Our Values
            </h2>
            <div className="features-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
              <div>
                <h3 style={{ color: '#10b981', fontWeight: 600, marginBottom: '0.5rem' }}>Transparency</h3>
                <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>We clearly communicate our methodology and confidence levels for every analysis.</p>
              </div>
              <div>
                <h3 style={{ color: '#10b981', fontWeight: 600, marginBottom: '0.5rem' }}>Education</h3>
                <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>We provide educational insights to help you understand the reasoning behind our recommendations.</p>
              </div>
              <div>
                <h3 style={{ color: '#10b981', fontWeight: 600, marginBottom: '0.5rem' }}>Data-Driven</h3>
                <p style={{ color: '#a3a3a3', fontSize: '0.9rem' }}>All our recommendations are based on quantitative analysis, not speculation or hype.</p>
              </div>
            </div>
          </div>

          <div className="prop-card">
            <h2 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
              Disclaimer
            </h2>
            <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
              <strong>Important:</strong> TrueEquity provides educational and informational content only. Our analysis is not financial advice. 
              All investment decisions are your own responsibility. Past performance does not guarantee future results. 
              Always do your own research and consider consulting with a qualified financial advisor before making investment decisions.
            </p>
          </div>
        </div>
      </main>
    </>
  );
}
