'use client';

import { useState } from 'react';
import Link from 'next/link';

export default function PricingPage() {
  const [billingPeriod, setBillingPeriod] = useState<'monthly' | 'annual'>('monthly');

  const plans = {
    starter: {
      monthly: { price: '$0', period: 'per user/month' },
      annual: { price: '$0', period: 'per user/year' },
      description: 'Ideal for individuals who want to get started with simple stock analysis.',
      features: [
        '5 stock lookups per month',
        'Basic BUY/HOLD/SELL suggestions',
        'Financial snapshot',
        'Email support'
      ]
    },
    pro: {
      monthly: { price: '$50', period: 'per user/month' },
      annual: { price: '$500', period: 'per user/year' },
      description: 'Enhanced design tools for scaling teams who need more flexibility.',
      features: [
        'Unlimited stock lookups',
        'Advanced analysis & scenarios',
        'Watchlist & alerts',
        'Market regime indicator',
        'Priority support'
      ]
    },
    advanced: {
      monthly: { price: '$85', period: 'per user/month' },
      annual: { price: '$850', period: 'per user/year' },
      description: 'Powerful tools designed for extensive collaboration and customization.',
      features: [
        'Everything in Pro',
        'API access',
        'Custom reports',
        'Dedicated account manager',
        'Single sign on (SSO)'
      ]
    }
  };

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
          <li><Link href="/pricing">Pricing</Link></li>
          <li><Link href="/about">About</Link></li>
          <li><Link href="/#faq">FAQ</Link></li>
        </ul>
      </nav>

      <section className="pricing-section" id="pricing">
        <div className="pricing-header">
          <h2 className="pricing-title">Powerful design tools.<br />Simple pricing.</h2>
          <p className="pricing-subtitle">Plans for teams of every size — from start-up to enterprise.</p>
          
          <div className="billing-toggle">
            <button 
              className={`toggle-btn ${billingPeriod === 'monthly' ? 'active' : ''}`}
              onClick={() => setBillingPeriod('monthly')}
            >
              Monthly
            </button>
            <button 
              className={`toggle-btn ${billingPeriod === 'annual' ? 'active' : ''}`}
              onClick={() => setBillingPeriod('annual')}
            >
              Annual
            </button>
          </div>
        </div>

        <div className="pricing-grid">
          {/* Starter Plan */}
          <div className="pricing-card">
            <div className="plan-header">
              <div className="plan-icon">
                <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M14 10l-2 1m0 0l-2-1m2 1v2.5M20 7l-2 1m2-1l-2-1m2 1v2.5M14 4l-2-1-2 1M4 7l2-1M4 7l2 1M4 7v2.5M12 21l-2-1m2 1l2-1m-2 1v-2.5M6 18l-2-1v-2.5M18 18l2-1v-2.5"/>
                </svg>
              </div>
              <span className="plan-name">Starter</span>
            </div>

            <div className="plan-price">{plans.starter[billingPeriod].price}</div>
            <div className="plan-period">{plans.starter[billingPeriod].period}</div>

            <p className="plan-description">{plans.starter.description}</p>

            <ul className="plan-features">
              {plans.starter.features.map((feature, index) => (
                <li key={index}>
                  <div className="check-icon">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7"/>
                    </svg>
                  </div>
                  {feature}
                </li>
              ))}
            </ul>

            <button className="plan-cta">Get Started</button>
          </div>

          {/* Pro Plan */}
          <div className="pricing-card popular">
            <span className="popular-badge">Most popular</span>
            
            <div className="plan-header">
              <div className="plan-icon">
                <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/>
                </svg>
              </div>
              <span className="plan-name">Pro</span>
            </div>

            <div className="plan-price">{plans.pro[billingPeriod].price}</div>
            <div className="plan-period">{plans.pro[billingPeriod].period}</div>

            <p className="plan-description">{plans.pro.description}</p>

            <ul className="plan-features">
              {plans.pro.features.map((feature, index) => (
                <li key={index}>
                  <div className="check-icon">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7"/>
                    </svg>
                  </div>
                  {feature}
                </li>
              ))}
            </ul>

            <button className="plan-cta">Get Started</button>
          </div>

          {/* Advanced Plan */}
          <div className="pricing-card">
            <div className="plan-header">
              <div className="plan-icon">
                <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
              </div>
              <span className="plan-name">Advanced</span>
            </div>

            <div className="plan-price">{plans.advanced[billingPeriod].price}</div>
            <div className="plan-period">{plans.advanced[billingPeriod].period}</div>

            <p className="plan-description">{plans.advanced.description}</p>

            <ul className="plan-features">
              {plans.advanced.features.map((feature, index) => (
                <li key={index}>
                  <div className="check-icon">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7"/>
                    </svg>
                  </div>
                  {feature}
                </li>
              ))}
            </ul>

            <button className="plan-cta">Get Started</button>
          </div>
        </div>
      </section>
    </>
  );
}
