'use client';

import { useState } from 'react';
import SearchBar from '@/components/SearchBar';
import Link from 'next/link';

export default function Home() {
  const [openFaq, setOpenFaq] = useState<number | null>(null);

  const toggleFaq = (index: number) => {
    setOpenFaq(openFaq === index ? null : index);
  };

  const faqs = [
    {
      question: 'How does TrueEquity calculate stock recommendations?',
      answer: 'TrueEquity uses a comprehensive scoring system that analyzes multiple factors including valuation metrics (P/E, PEG, Price-to-Book), financial health (debt levels, current ratio, profitability), growth potential (revenue growth, EPS growth), and risk assessment (volatility, market conditions). Each stock receives a BUY, HOLD, SELL, or AVOID recommendation based on these data-driven calculations.'
    },
    {
      question: 'How often is the stock data updated?',
      answer: 'Stock prices and volume are updated every 15 minutes during market hours (9:30 AM - 4:00 PM EST). Technical indicators like RSI are recalculated with each price update. Financial fundamentals (EPS, revenue, debt, etc.) are updated every 12 hours to ensure accuracy while minimizing API costs. All data is stored in our secure database for fast access.'
    },
    {
      question: 'Is TrueEquity\'s analysis financial advice?',
      answer: 'No. TrueEquity provides educational decision-support information only. Our recommendations are based on data analysis and should not be considered financial advice. Always conduct your own research, consult with a licensed financial advisor, and make investment decisions based on your own risk tolerance and financial situation. Past performance does not guarantee future results.'
    },
    {
      question: 'What data sources does TrueEquity use?',
      answer: 'TrueEquity uses a hybrid approach combining Yahoo Finance for real-time price data and Alpha Vantage for comprehensive financial fundamentals including income statements, balance sheets, cash flow statements, and company overviews. We calculate technical indicators like RSI ourselves using historical price data to ensure accuracy and consistency.'
    },
    {
      question: 'Can I use TrueEquity for free?',
      answer: 'Yes! TrueEquity offers a free Starter plan that includes 5 stock lookups per month, basic BUY/HOLD/SELL suggestions, financial snapshots, and email support. This is perfect for individuals getting started with stock analysis. For unlimited access and advanced features, check out our Pro and Advanced plans.'
    },
    {
      question: 'How accurate are the stock recommendations?',
      answer: 'Our recommendations are based on objective financial metrics and data analysis. However, stock markets are inherently unpredictable, and no system can guarantee accuracy. TrueEquity provides transparency by showing the reasoning behind each recommendation, including valuation scores, financial health metrics, growth potential, and risk factors. We recommend using our analysis as one tool among many in your investment research process.'
    }
  ];

  return (
    <>
      <div className="gradient-bg"></div>
      <div className="grid-overlay"></div>

      <nav>
        <div className="logo">
          <div className="logo-icon">T</div>
          TrueEquity
        </div>
        <ul className="nav-links">
          <li><Link href="/">Home</Link></li>
          <li><Link href="/stocks">Stocks</Link></li>
          <li><Link href="/pricing">Pricing</Link></li>
          <li><Link href="/#faq">FAQ</Link></li>
        </ul>
      </nav>

      <section className="hero" id="home">
        <div className="disclaimer-badge">
          Not financial advice • Educational purposes only
        </div>
        <h1>Honest stock insights.<br />No hype. No predictions.</h1>
        <p className="subtitle">Data-driven analysis with clear buy, hold, or sell suggestions based on fundamentals, growth, and risk.</p>
        
        <div className="search-container">
          <SearchBar />
        </div>
      </section>

      <section className="value-props">
        <div className="props-grid">
          <div className="prop-card">
            <div className="prop-icon">
              <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
            <h3>No Hype, Just Facts</h3>
            <p>We analyze fundamentals, cash flow, debt levels, and growth metrics. No sensational headlines or clickbait.</p>
          </div>

          <div className="prop-card">
            <div className="prop-icon">
              <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
            </div>
            <h3>Clear Suggestions</h3>
            <p>Every stock gets a simple recommendation: BUY, HOLD, SELL, or AVOID—backed by transparent reasoning.</p>
          </div>

          <div className="prop-card">
            <div className="prop-icon">
              <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
              </svg>
            </div>
            <h3>Risk Transparency</h3>
            <p>We clearly communicate confidence levels and potential scenarios—bull, base, and bear cases for every analysis.</p>
          </div>
        </div>
      </section>

      {/* Legal Notice */}
      <section style={{ padding: '2rem 0', textAlign: 'center' }}>
        <p className="legal-notice">This platform provides educational analysis. Always do your own research.</p>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="faq-section">
        <div className="faq-container">
          <h2 className="faq-title">Frequently Asked Questions</h2>
          <p className="faq-subtitle">Everything you need to know about TrueEquity</p>
          
          <div className="faq-list">
            {faqs.map((faq, index) => (
              <div key={index} className="faq-item">
                <button 
                  className="faq-question"
                  onClick={() => toggleFaq(index)}
                >
                  <span>{faq.question}</span>
                  <svg 
                    className={`faq-icon ${openFaq === index ? 'active' : ''}`}
                    fill="none" 
                    stroke="currentColor" 
                    strokeWidth="2" 
                    viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7"/>
                  </svg>
                </button>
                <div className={`faq-answer ${openFaq === index ? 'active' : ''}`}>
                  <p>{faq.answer}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <footer>
        <div className="footer-content">
          <div className="footer-brand">
            <div className="logo">
              <div className="logo-icon">T</div>
              TrueEquity
            </div>
            <p>Data-driven stock analysis platform providing transparent, educational insights for informed investment decisions.</p>
          </div>
          <div className="footer-column">
            <h4>Product</h4>
            <ul>
              <li><Link href="/features">Features</Link></li>
              <li><Link href="/pricing">Pricing</Link></li>
              <li><Link href="/docs">Documentation</Link></li>
            </ul>
          </div>
          <div className="footer-column">
            <h4>Company</h4>
            <ul>
              <li><Link href="/about">About</Link></li>
              <li><Link href="/contact">Contact</Link></li>
            </ul>
          </div>
          <div className="footer-column">
            <h4>Legal</h4>
            <ul>
              <li><Link href="/privacy">Privacy Policy</Link></li>
              <li><Link href="/terms">Terms of Service</Link></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          <div className="disclaimer-text">
            <strong>Disclaimer:</strong> This is not financial advice. TrueEquity provides decision-support information only. All investment decisions are your own responsibility. Past performance does not guarantee future results.
          </div>
          <p style={{ marginTop: '1rem' }}>© 2025 TrueEquity. All rights reserved.</p>
        </div>
      </footer>
    </>
  );
}
