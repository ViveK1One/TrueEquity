import Link from 'next/link';

export default function TermsPage() {
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
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '0 2rem' }}>
          <h1 style={{ fontSize: '3rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa', textAlign: 'center' }}>
            Terms of Service
          </h1>
          <p style={{ fontSize: '1rem', color: '#a3a3a3', textAlign: 'center', marginBottom: '3rem' }}>
            Last Updated: January 2025
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                1. Acceptance of Terms
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                By accessing and using TrueEquity ("the Service"), you accept and agree to be bound by the terms and provision of this agreement. 
                If you do not agree to these Terms of Service, please do not use our Service.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                2. Description of Service
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                TrueEquity is an educational platform that provides stock analysis, financial data, and investment insights. 
                Our Service includes stock search, financial metrics, technical indicators, and investment recommendations based on quantitative analysis.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                3. Not Financial Advice
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                <strong>IMPORTANT DISCLAIMER:</strong> TrueEquity provides educational and informational content only. Our analysis, recommendations, 
                and data are NOT financial advice. We are not licensed financial advisors, brokers, or investment advisors.
              </p>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                All investment decisions are your own responsibility. You should conduct your own research and consult with a qualified financial 
                advisor before making any investment decisions. Past performance does not guarantee future results.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                4. User Responsibilities
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                You agree to:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li>Use the Service only for lawful purposes</li>
                <li>Not attempt to gain unauthorized access to our systems</li>
                <li>Not use the Service to violate any applicable laws or regulations</li>
                <li>Not interfere with or disrupt the Service or servers</li>
                <li>Not reproduce, duplicate, or copy any portion of the Service without permission</li>
                <li>Understand that all investment decisions are your own responsibility</li>
              </ul>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                5. Data Accuracy and Availability
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                While we strive to provide accurate and up-to-date information, we do not guarantee the accuracy, completeness, or timeliness of any 
                data or analysis provided through the Service. Stock market data is subject to change, and we are not responsible for any errors or 
                omissions. The Service may be unavailable at times due to maintenance or technical issues.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                6. Intellectual Property
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                All content, features, and functionality of the Service, including but not limited to text, graphics, logos, and software, 
                are the property of TrueEquity or its licensors and are protected by copyright, trademark, and other intellectual property laws. 
                You may not use our content without our express written permission.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                7. Limitation of Liability
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7', marginBottom: '1rem' }}>
                TO THE MAXIMUM EXTENT PERMITTED BY LAW, TRUEEQUITY SHALL NOT BE LIABLE FOR:
              </p>
              <ul style={{ color: '#a3a3a3', lineHeight: '1.7', marginLeft: '2rem' }}>
                <li>Any investment losses or damages resulting from your use of the Service</li>
                <li>Any errors, omissions, or inaccuracies in the data or analysis provided</li>
                <li>Any interruption or cessation of the Service</li>
                <li>Any indirect, incidental, special, consequential, or punitive damages</li>
                <li>Any loss of profits, revenue, data, or use</li>
              </ul>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                8. Indemnification
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                You agree to indemnify, defend, and hold harmless TrueEquity and its officers, directors, employees, and agents from any claims, 
                damages, losses, liabilities, and expenses (including legal fees) arising from your use of the Service or violation of these Terms.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                9. Service Modifications
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We reserve the right to modify, suspend, or discontinue the Service (or any part thereof) at any time, with or without notice. 
                We shall not be liable to you or any third party for any modification, suspension, or discontinuation of the Service.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                10. Third-Party Services
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                Our Service may contain links to third-party websites or services. We are not responsible for the content, privacy policies, 
                or practices of any third-party sites. Your use of third-party services is at your own risk.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                11. Termination
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We may terminate or suspend your access to the Service immediately, without prior notice, for any reason, including if you breach 
                these Terms. Upon termination, your right to use the Service will cease immediately.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                12. Governing Law
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                These Terms shall be governed by and construed in accordance with applicable laws, without regard to conflict of law provisions. 
                Any disputes arising from these Terms or your use of the Service shall be resolved in the appropriate courts.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                13. Changes to Terms
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                We reserve the right to modify these Terms at any time. We will notify users of any material changes by posting the updated Terms 
                on this page and updating the "Last Updated" date. Your continued use of the Service after such changes constitutes acceptance of the updated Terms.
              </p>
            </div>

            <div className="prop-card">
              <h2 style={{ fontSize: '1.75rem', fontWeight: 700, marginBottom: '1rem', color: '#fafafa' }}>
                14. Contact Information
              </h2>
              <p style={{ color: '#a3a3a3', lineHeight: '1.7' }}>
                If you have questions about these Terms of Service, please contact us through our 
                <Link href="/contact" style={{ color: '#60a5fa', textDecoration: 'underline', marginLeft: '0.25rem' }}>Contact</Link> page.
              </p>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
